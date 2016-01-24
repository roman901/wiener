package crypto.uwtech.org;

import java.math.BigInteger;

public class KeyDetails {

    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;

    public KeyDetails(BigInteger modulus, BigInteger privateKey, BigInteger publicKey) {
        this.modulus = modulus;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }
}
