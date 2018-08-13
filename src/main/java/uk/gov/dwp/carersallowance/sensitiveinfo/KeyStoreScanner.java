package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Scan files for specific file signatures.
 * If a file has a sensitive information file signature flag it as sensitive information
 *
 * The specific file signatures are taken from the "magic" database in
 * /etc/magic, /usr/share/file/magic or similar.
 * The specific signatures are:
 *  java
 *     Java KeyStore
 *     Java JCE KeyStore
 *  gnu
 *      GPG key trust database
 *  gnome-keyring
 *      GNOME keyring
 *  pgp
 *      PGP key security ring
 *      PGP signature
 *
 * @author David Hutchinson (drh@elegantsolutions.co.uk) on 21 Feb 2017.
 */
public class KeyStoreScanner extends AbstractScanner {
    private static enum START_BYTES_SIGNATURES {
        JAVA_KEYSTORE("Java KeyStore", "0xfeedfeed"),               // 4 bytes
        JAVA_JCE_KEYSTORE("Java JCE KeyStore", "0xcececece"),       // 4 bytes
        PGP_KEY_SECURITY_RING("PGP key security ring", "0x9501"),   // 2 bytes
        PGP_SIGNATURE("PGP signature", "0x9500");                   // 2 bytes

        public final static int LONGEST_SIGNATURE  = 4;

        private String fileType;
        private byte[] magicSignature;

        private START_BYTES_SIGNATURES(String fileType, String magicSignature) {
            this.fileType = fileType;
            this.magicSignature = parseHexBytes(magicSignature);
        }

        public String getFileType()         { return fileType; }
        public byte[] getMagicSignature()   { return magicSignature; }

        private static byte[] parseHexBytes(String string) {
            if(string == null) {
                return null;
            }

            if(string.startsWith("0x") || string.startsWith("0X")) {
                string = string.substring(2);
            }

            byte[] result = new byte[string.length() * 2];    // this is the max is could  be
            int resultIndex = 0;
            for(int index = 0; index < string.length(); index += 2) {
                String hexByte = string.substring(index, index + 2);
                result[resultIndex] = Integer.valueOf(hexByte,  16).byteValue();
                resultIndex++;
            }

            byte[] trimmed = Arrays.copyOf(result, resultIndex);
            return trimmed;
        }

        /**
         * @param firstFourBytes
         * @param throwExceptions used to support speculative matching
         * @return
         */
        public static START_BYTES_SIGNATURES valueOf(byte[] firstFourBytes, boolean throwExceptions) {
            if(firstFourBytes == null) {
                throw new IllegalArgumentException("firstFourBytes cannot be null");
            }
            if(firstFourBytes.length < 4) {
                throw new IllegalArgumentException("firstFourBytes must be four bytes or longer");
            }

            for(START_BYTES_SIGNATURES signature: START_BYTES_SIGNATURES.values()) {
                if(startsWith(firstFourBytes, signature.getMagicSignature())) {
                    return signature;
                }
            }

            if(throwExceptions) {
                String byteString = String.format("0x%X%X%X%X", firstFourBytes[0], firstFourBytes[1], firstFourBytes[2], firstFourBytes[3] );
                throw new IllegalArgumentException("unrecognised start bytes: " + byteString);
            }

            return null;
        }

        /**
         * Compares the start of 'value' with 'compare'
         *
         * @return true if they match, false otherwise
         */
        private static boolean startsWith(byte[] value, byte[] compare) {
            if(value == compare) {
                return true;
            }

            if(value == compare) {
                return false;
            }

            if(value.length < compare.length) {
                return false;
            }

            for(int index = 0; index < compare.length; index++) {
                if(value[index] != compare[index]) {
                    return false;
                }
            }

            return true;
        }
    }

    private static enum START_STRING_SIGNATURES {
        GPG_KEY_TRUST_DATABASE("GPG key trust database", "\001gpg"),
        GNOME_KEYRING("GNOME keyring", "GnomeKeyring\n\r\0\n"),
        PGP_SIGNATURE("PGP signature", "-----BEGIN\040PGP\40SIGNATURE-"),
        RSA_PRIVATE_KEY("RSA Private Key", "-----BEGIN RSA PRIVATE KEY-");

        private static final String UTF_16BE = "UTF_16BE";

        public final static int SHORTEST_SIGNATURE = "\001gpg".length();
        public final static int LONGEST_SIGNATURE  = "-----BEGIN\040PGP\40SIGNATURE-".length();

        private String fileType;
        private String magicSignature;

        private START_STRING_SIGNATURES(String fileType, String magicSignature) {
            this.fileType = fileType;
            this.magicSignature = magicSignature;
        }

        public String getFileType()            { return fileType; }
        public String getMagicSignature()      { return magicSignature; }

        public static START_STRING_SIGNATURES valueOf(byte[] startOfFile, boolean throwExceptions) {
            if(startOfFile == null) {
                throw new IllegalArgumentException("startOfFile cannot be null");
            }

            try {
                String string = new String(startOfFile, 0, startOfFile.length - startOfFile.length%2, UTF_16BE);
                return valueOf(string, throwExceptions);
            } catch (UnsupportedEncodingException e) {
                // we should never see this as UTF_16BE is one of the built in charsets
                throw new IllegalStateException("UTF_16BE charset not supported");
            }
        }

        /**
         * @param firstFourBytes
         * @param throwExceptions used to support speculative matching
         * @return
         */
        public static START_STRING_SIGNATURES valueOf(String startOfFile, boolean throwExceptions) {
            if(startOfFile == null) {
                throw new IllegalArgumentException("startOfFile cannot be null");
            }

            if(startOfFile.length() < SHORTEST_SIGNATURE) {
                throw new IllegalArgumentException("startOfFile must be at least " + SHORTEST_SIGNATURE + " bytes long, but need not be longer than " + LONGEST_SIGNATURE + " long");
            }

            for(START_STRING_SIGNATURES signature: START_STRING_SIGNATURES.values()) {
                if(startOfFile.startsWith(signature.getMagicSignature())) {
                    return signature;
                }
            }

            if(throwExceptions) {
                String errorMsg = "unrecognised start string: " + startOfFile.substring(0, Math.min(startOfFile.length(), LONGEST_SIGNATURE));
                if(startOfFile.length() > LONGEST_SIGNATURE) {
                    errorMsg += " ...";
                }
                throw new IllegalArgumentException(errorMsg);
            }

            return null;
        }
    }

    public KeyStoreScanner() {
    }

    public KeyStoreScanner(JsonNode configNode) {
        this();

        // there is no config for the KeyStoreScanner
    }

    @Override
    public List<SensitiveInformation> scan(File baseDir) throws IOException {
        List<SensitiveInformation> results = new ArrayList<>();
        scan(results, baseDir);
        return results;
    }

    private void scan(List<SensitiveInformation> results, File file) {
        assert results != null;
        if(file == null) {
            return;
        }

        if(file.isDirectory()) {
            File[] children = file.listFiles();
            for(File child: children) {
                scan(results, child);
            }
        } else {
            int maxBytesToRead = Math.max(START_BYTES_SIGNATURES.LONGEST_SIGNATURE, START_STRING_SIGNATURES.LONGEST_SIGNATURE);
            byte[] bytes;
            try {
                bytes = readFileStart(file, maxBytesToRead);
            } catch(IOException e) {
                results.add(new SensitiveInformation("Unable to read start of file: " + file.getAbsolutePath()));
                return;
            }

            START_BYTES_SIGNATURES sensitiveBytesFile = START_BYTES_SIGNATURES.valueOf(bytes, false);
            if(sensitiveBytesFile != null) {
                results.add(new SensitiveInformation("Found sensitive file(" + sensitiveBytesFile.getFileType() + "): " + file.getAbsolutePath()));
                return;
            }

            START_STRING_SIGNATURES sensitiveStringFile = START_STRING_SIGNATURES.valueOf(bytes, false);
            if(sensitiveStringFile != null) {
                results.add(new SensitiveInformation("Found sensitive file(" + sensitiveStringFile.getFileType() + "): " + file.getAbsolutePath()));
                return;
            }
        }
    }

    private byte[] readFileStart(File file, int maxBytesToRead) throws IOException {
        if(file.exists() == false) {
            return null;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] results = new byte[maxBytesToRead];
            int read = inputStream.read(results);
            if(read < results.length) {
                byte[] tmp = Arrays.copyOf(results, read);
                return tmp;
            }
            return results;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static void main(String[] args) throws IOException {
        String dir = "/Users/drh/release/testcommons/src/main/resources/sslsettings";
        KeyStoreScanner scanner = new KeyStoreScanner();
        List<SensitiveInformation> results = scanner.scan(new File(dir));
        System.out.println(results);
    }
}
/*
0   belong      0xfeedfeed  Java KeyStore
0   belong      0xcececece  Java JCE KeyStore

0   string          \001gpg                 GPG key trust database

0   beshort         0x9501                  PGP key security ring
0   beshort         0x9500                  PGP key security ring

0   string   GnomeKeyring\n\r\0\n GNOME keyring

0   string  -----BEGIN\040PGP\40SIGNATURE-      PGP signature

0   string  -----BEGIN RSA PRIVATE KEY      RSA private key (not in magic database)
*/
