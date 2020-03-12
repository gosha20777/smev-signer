package smevsign;

import static smevsign.Resources.SMEV_TRANSFORM_URN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;

/**
 * @author gosha20777
 */
public final class SmevTransformSpi extends TransformSpi {

	private static final String NS_PREFIX = "ns";
	private static final String ENCODING_UTF_8 = "UTF-8";

	private static AttributeSortingComparator attributeSortingComparator = new AttributeSortingComparator();

	private static ThreadLocal<XMLInputFactory> inputFactory =
			new ThreadLocal<XMLInputFactory>() {
				@Override
				protected XMLInputFactory initialValue() {
					return XMLInputFactory.newInstance();
				}
			};

	private static ThreadLocal<XMLOutputFactory> outputFactory =
			new ThreadLocal<XMLOutputFactory>() {
				@Override
				protected XMLOutputFactory initialValue() {
					return XMLOutputFactory.newInstance();
				}
			};

	private static ThreadLocal<XMLEventFactory> eventFactory =
			new ThreadLocal<XMLEventFactory>() {
				@Override
				protected XMLEventFactory initialValue() {
					return XMLEventFactory.newInstance();
				}
			};

	@Override
	protected String engineGetURI() {
		return SMEV_TRANSFORM_URN;
	}

	@Override
	protected XMLSignatureInput enginePerformTransform(XMLSignatureInput argInput, OutputStream argOutput, Transform argTransform)
			throws IOException, TransformationException {

		process(argInput.getOctetStream(), argOutput);
		XMLSignatureInput result = new XMLSignatureInput((byte[]) null);
		result.setOutputStream(argOutput);
		return result;
	}

	@Override
	protected XMLSignatureInput enginePerformTransform(XMLSignatureInput argInput, Transform argTransform)
			throws IOException, TransformationException {
		return enginePerformTransform(argInput);
	}

	@Override
	protected XMLSignatureInput enginePerformTransform(XMLSignatureInput argInput)
			throws IOException, TransformationException {

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		process(argInput.getOctetStream(), result);
		byte[] postTransformData = result.toByteArray();

		return new XMLSignatureInput(postTransformData);
	}

	public void process(InputStream argSrc, OutputStream argDst) throws TransformationException {
		Stack<List<Namespace>> prefixMappingStack = new Stack<List<Namespace>>();
		int prefixCnt = 1;
		XMLEventReader src = null;
		XMLEventWriter dst = null;
		try {
			src = inputFactory.get().createXMLEventReader(argSrc, ENCODING_UTF_8);
			dst = outputFactory.get().createXMLEventWriter(argDst, ENCODING_UTF_8);

			XMLEventFactory factory = eventFactory.get();

			while (src.hasNext()) {
				XMLEvent event = src.nextEvent();

				if (event.isCharacters()) {
					String data = event.asCharacters().getData();
					// Отсекаем возвраты каретки и пробельные строки.
					if (!data.trim().isEmpty()) {
						dst.add(event);
					}
					//continue;
				} else if (event.isStartElement()) {
					List<Namespace> myPrefixMappings = new LinkedList<Namespace>();
					prefixMappingStack.push(myPrefixMappings);

					// Обработка элемента: NS prefix rewriting.
					// N.B. Элементы в unqualified form не поддерживаются.
					StartElement srcEvent = (StartElement) event;
					String nsURI = srcEvent.getName().getNamespaceURI();
					String prefix = findPrefix(nsURI, prefixMappingStack);

					if (prefix == null) {
						prefix = NS_PREFIX + String.valueOf(prefixCnt++);
						myPrefixMappings.add(factory.createNamespace(prefix, nsURI));
					}
					StartElement dstEvent = factory.createStartElement(
							prefix, nsURI, srcEvent.getName().getLocalPart());
					dst.add(dstEvent);

					// Обработка атрибутов. Два шага: отсортировать, промэпить namespace URI.

					@SuppressWarnings("unchecked") Iterator<Attribute> srcAttributeIterator = srcEvent.getAttributes();
					// Положим атрибуты в list, чтобы их можно было отсортировать.
					List<Attribute> srcAttributeList = new LinkedList<Attribute>();
					while (srcAttributeIterator.hasNext()) {
						srcAttributeList.add(srcAttributeIterator.next());
					}
					// Сортировка атрибутов по алфавиту.
					Collections.sort(srcAttributeList, attributeSortingComparator);

					// Обработка префиксов. Аналогична обработке префиксов элементов,
					// за исключением того, что у атрибут может не иметь namespace.
					List<Attribute> dstAttributeList = new LinkedList<Attribute>();
					for (Attribute srcAttribute : srcAttributeList) {
						String attributeNsURI = srcAttribute.getName().getNamespaceURI();
						String attributeLocalName = srcAttribute.getName().getLocalPart();
						String value = srcAttribute.getValue();
						String attributePrefix = null;
						Attribute dstAttribute = null;
						if (attributeNsURI != null && !"".equals(attributeNsURI)) {
							attributePrefix = findPrefix(attributeNsURI, prefixMappingStack);
							if (attributePrefix == null) {
								attributePrefix = NS_PREFIX + String.valueOf(prefixCnt++);
								myPrefixMappings.add(factory.createNamespace(
										attributePrefix, attributeNsURI));
							}
							dstAttribute = factory.createAttribute(
									attributePrefix, attributeNsURI, attributeLocalName, value);
						} else {
							dstAttribute = factory.createAttribute(attributeLocalName, value);
						}
						dstAttributeList.add(dstAttribute);
					}
					// Высести namespace prefix mappings для текущего элемента.
					// их порядок детерминирован, т.к. перед мэппингом атрибуты
					// были отсортированы.
					// Поэтому дополнительной сотрировки здесь не нужно.
					for (Namespace mapping : myPrefixMappings) {
						dst.add(mapping);
					}
					// Вывести атрибуты.
					// N.B. Мы не выводим атрибуты сразу вместе с элементом, используя метод
					// XMLEventFactory.createStartElement(prefix, nsURI, localName,
					//   List<Namespace>, List<Attribute>),
					// потому что при использовании этого метода порядок атрибутов
					// в выходном документе меняется произвольным образом.
					for (Attribute attr : dstAttributeList) {
						dst.add(attr);
					}
					//continue;
				} else if (event.isEndElement()) {
					// Гарантируем, что empty tags запишутся в форме <a></a>, а не в форме <a/>.
					dst.add(eventFactory.get().createSpace(""));

					// NS prefix rewriting
					EndElement srcEvent = (EndElement) event;
					String nsURI = srcEvent.getName().getNamespaceURI();
					String prefix = findPrefix(nsURI, prefixMappingStack);
					if (prefix == null) {
						throw new TransformationException(
								"EndElement: prefix mapping is not found for namespace " + nsURI);
					}

					EndElement dstEvent = eventFactory.get().
							createEndElement(prefix, nsURI, srcEvent.getName().getLocalPart());
					dst.add(dstEvent);

					prefixMappingStack.pop(); // bugfixes

					//continue;
				}/* else if (event.isAttribute()) { //Атрибуты обрабатываются в событии startElement.
					//continue;
				}*/

				// Остальные события (processing instructions, start document, etc.) нас не интересуют
			}
		} catch (XMLStreamException e) {
			Object exArgs[] = {e.getMessage()};
			throw new TransformationException(
					"Can not perform transformation " + SMEV_TRANSFORM_URN, exArgs, e);
		} finally {
			if (src != null) {
				try {
					src.close();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
			if (dst != null) {
				try {
					dst.close();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
			try {
				argSrc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				argDst.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String findPrefix(String argNamespaceURI, Stack<List<Namespace>> argMappingStack) {
		if (argNamespaceURI == null) {
			throw new IllegalArgumentException("No namespace элементы не поддерживаются.");
		}

		for (List<Namespace> elementMappingList : argMappingStack) {
			for (Namespace mapping : elementMappingList) {
				if (argNamespaceURI.equals(mapping.getNamespaceURI())) {
					return mapping.getPrefix();
				}
			}
		}
		return null;
	}

	private static class AttributeSortingComparator implements Comparator<Attribute> {
		@Override
		public int compare(Attribute x, Attribute y) {
			String xNS = x.getName().getNamespaceURI();
			String xLocal = x.getName().getLocalPart();
			String yNS = y.getName().getNamespaceURI();
			String yLocal = y.getName().getLocalPart();

			// Сначала сравниваем namespaces.
			if (xNS == null || xNS.equals("")) {
				if (yNS != null && !"".equals(xNS)) {
					return 1;
				}
			} else {
				if (yNS == null || "".equals(yNS)) {
					return -1;
				} else {
					int nsComparisonResult = xNS.compareTo(yNS);
					if (nsComparisonResult != 0) {
						return nsComparisonResult;
					}
				}
			}
			// Если namespaces признаны эквивалентными, сравниваем local names.
			return xLocal.compareTo(yLocal);
		}
	}
}
