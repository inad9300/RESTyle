<?php

namespace App\Utils;

class Arrays {

    public static function isAssoc($arr) {
        return is_array($arr)
            && array_keys($arr) !== range(0, count($arr) - 1);
    }
}