package smevsign;

public interface Resources {
	String XMLDSIG_MORE_GOSTR34102001_GOSTR3411 = "urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34102012-gostr34112012-256";
	String XMLDSIG_MORE_GOSTR3411 = "urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34112012-256";
	String CANONICALIZATION_METHOD = "http://www.w3.org/2001/10/xml-exc-c14n#";
	String SMEV_TRANSFORM_URN = "urn://smev-gov-ru/xmldsig/transform";
	String JCP_STORE_NAME = "HDImageStore";
	String SIGNED_BY_CONSUMER = "SIGNED_BY_CONSUMER"; //ID элемента в XML на который следует поставить подпись
	String SIGNED_BY_CALLER = "SIGNED_BY_CALLER"; //ID элемента в XML на который следует поставить подпись
	String CALLER_INFORMATION_SYSTEM_SIGNATURE = "CallerInformationSystemSignature"; //имя элемента в XML без префикса в который следует добавить подпись
	String CHARGE_TARGET_NAMESPACE = "urn://roskazna.ru/gisgmp/xsd/services/import-charges/2.0.1";
	String CHARGE_LOCAL_NAME = "ImportChargesRequest";
	String PAYMENT_TARGET_NAMESPACE = "urn://roskazna.ru/gisgmp/xsd/services/export-payments/2.0.1";
	String PAYMENT_LOCAL_NAME = "ExportPaymentsRequest";
}