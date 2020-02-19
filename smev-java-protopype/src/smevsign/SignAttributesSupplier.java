package smevsign;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface SignAttributesSupplier {
	X509Certificate x509Certificate() throws Exception; //сертификат открытого ключа проверки подписи
	PrivateKey privateKey() throws Exception; //закрытый ключ подписи
	String forSignElementId(); //ID элемента в XML на который следует поставить подпись
}