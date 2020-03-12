package smevsign;

import static smevsign.KeyStoreWrapper.getPrivateKey;
import static smevsign.KeyStoreWrapper.getX509Certificate;
import static smevsign.Resources.SIGNED_BY_CALLER;
import static smevsign.Resources.SIGNED_BY_CONSUMER;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class SupplierFactory {

	public static SignAttributesSupplier signedByConsumer(final KeyHolder admin){
		return create(admin, SIGNED_BY_CONSUMER);
	}

	public static SignAttributesSupplier signedByCaller(final KeyHolder admin){
		return create(admin, SIGNED_BY_CALLER);
	}

	private static SignAttributesSupplier create(final KeyHolder admin, final String signedBy){
		return new SignAttributesSupplier() {
			@Override
			public X509Certificate x509Certificate() throws Exception {
				return getX509Certificate(admin.getKeyAlias());
			}

			@Override
			public PrivateKey privateKey() throws Exception {
				return getPrivateKey(admin.getKeyAlias(), admin.getKeyPassword().toCharArray());
			}

			@Override
			public String forSignElementId() {
				return signedBy;
			}
		};
	}
}