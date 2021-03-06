package org.bitcoinj.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cryptohash.BLAKE512;
import fr.cryptohash.BMW512;
import fr.cryptohash.CubeHash512;
import fr.cryptohash.ECHO512;
import fr.cryptohash.Fugue512;
import fr.cryptohash.Groestl512;
import fr.cryptohash.Hamsi512;
import fr.cryptohash.JH512;
import fr.cryptohash.Keccak512;
import fr.cryptohash.Luffa512;
import fr.cryptohash.SHAvite512;
import fr.cryptohash.SIMD512;
import fr.cryptohash.Skein512;

public class X13 {

    private static final Logger log = LoggerFactory.getLogger(X13.class);
    private static boolean native_library_loaded = false;

    static {

        try {
            log.info("Loading x13 native library...");
            System.loadLibrary("x13");
            native_library_loaded = true;
            log.info("Loaded x13 successfully.");
        }
        catch(UnsatisfiedLinkError x)
        {
            log.info("Loading x13 failed: " + x.getMessage());
        }
        catch(Exception e)
        {
            native_library_loaded = false;
            log.info("Loading x13 failed: " + e.getMessage());
        }
    }

    public static byte[] x13Digest(byte[] input, int offset, int length)
    {
        byte [] buf = new byte[length];
        for(int i = 0; i < length; ++i)
        {
            buf[i] = input[offset + i];
        }
        return x13Digest(buf);
    }

    public static byte[] x13Digest(byte[] input) {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? x13_native(input) : x13(input);
        /*long start = System.currentTimeMillis();
        byte [] result = x13_native(input);
        long end1 = System.currentTimeMillis();
        byte [] result2 = x13(input);
        long end2 = System.currentTimeMillis();
        log.info("x13: native {} / java {}", end1-start, end2-end1);
        return result;*/
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("x13 Hash time: {} ms per block", time);
        }
    }

    static native byte [] x13_native(byte [] input);


    static byte [] x13(byte header[])
    {
        //Initialize
        Sha512Hash[] hash = new Sha512Hash[17];

        //Run the chain of algorithms
        BLAKE512 blake512 = new BLAKE512();
        hash[0] = new Sha512Hash(blake512.digest(header));

        BMW512 bmw = new BMW512();
        hash[1] = new Sha512Hash(bmw.digest(hash[0].getBytes()));

        Groestl512 groestl = new Groestl512();
        hash[2] = new Sha512Hash(groestl.digest(hash[1].getBytes()));

        Skein512 skein = new Skein512();
        hash[3] = new Sha512Hash(skein.digest(hash[2].getBytes()));

        JH512 jh = new JH512();
        hash[4] = new Sha512Hash(jh.digest(hash[3].getBytes()));

        Keccak512 keccak = new Keccak512();
        hash[5] = new Sha512Hash(keccak.digest(hash[4].getBytes()));

        Luffa512 luffa = new Luffa512();
        hash[6] = new Sha512Hash(luffa.digest(hash[5].getBytes()));

        CubeHash512 cubehash = new CubeHash512();
        hash[7] = new Sha512Hash(cubehash.digest(hash[6].getBytes()));

        SHAvite512 shavite = new SHAvite512();
        hash[8] = new Sha512Hash(shavite.digest(hash[7].getBytes()));

        SIMD512 simd = new SIMD512();
        hash[9] = new Sha512Hash(simd.digest(hash[8].getBytes()));

        ECHO512 echo = new ECHO512();
        hash[10] = new Sha512Hash(echo.digest(hash[9].getBytes()));

        Hamsi512 hamsi = new Hamsi512();
        hash[11] = new Sha512Hash(hamsi.digest(hash[10].getBytes()));

        Fugue512 fugue = new Fugue512();
        hash[12] = new Sha512Hash(fugue.digest(hash[11].getBytes()));

        return hash[12].trim256().getBytes();
    }
}
