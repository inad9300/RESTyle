<?php

namespace App\Utils;

class Security {

    // Source: https://alias.io/2010/01/store-passwords-safely-with-php-and-mysql
    // NOTE: requires PHP's Mcrypt extension (http://php.net/manual/es/book.mcrypt.php)

    // Used to treat "encrypted" fields
    public static function hash($str) {
        // A higher "cost" is more secure but consumes more processing power
        $cost = 10;

        // Create a random salt
        $salt = strtr(base64_encode(mcrypt_create_iv(16, MCRYPT_DEV_URANDOM)), '+', '.');

        // Prefix information about the hash so PHP knows how to verify it later.
        // "$2a$" Means we're using the Blowfish algorithm. The following two digits are the cost parameter.
        $salt = sprintf("$2a$%02d$", $cost) . $salt;

        // Hash the string with the salt
        return crypt($str, $salt);
    }

    public static function verify($plain, $hash) {
        // Hashing the password with its hash as the salt returns the same hash
        return hash_equals($hash, crypt($plain, $hash));
    }
}