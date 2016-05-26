<?php

namespace App\Utils;

class Arrays {

    public static function isAssoc($arr) {
        return is_array($arr)
            && array_keys($arr) !== range(0, count($arr) - 1);
    }

	public static function renameKey($old, $new, &$arr) {
        if (gettype($arr) === 'array') {
            if (array_key_exists($old, $arr)) {
                $arr[$new] = $arr[$old];
                unset($arr[$old]);
                return true;
            } else {
                return false;
            }
        }
        else if (gettype($arr) === 'object') {
            if (property_exists($arr, $old) || isset($arr->{$old})) {
                $arr->{$new} = $arr->{$old};
                unset($arr->{$old});
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }
}