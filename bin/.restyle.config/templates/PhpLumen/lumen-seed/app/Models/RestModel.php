<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class RestModel extends Model {

    protected $dateFormat = 'Y-m-d\TH:i:sP'; // ISO 8601 format

    protected $fileUrlPart = '/files';

    public $timestamps = false;

    protected $fillable = [];

    protected $casts = [];

    protected static $validationRules = [];

    protected static $filterable = [];

    protected static $sortable = [];

    public static function getValidationRules() {
        return static::$validationRules;
    }

    public static function getFilterable() {
        return static::$filterable;
    }

    public static function getSortable() {
        return static::$sortable;
    }

    public static function isFilterable($field) {
        return in_array($field, static::$filterable);
    }

    public static function isSortable($field) {
        return in_array($field, static::$sortable);
    }

    /**
     * Get the date formatted according to the ISO 8601.
     */
    public static function getIsoUtcDate($str) {
        if (empty($str)) {
            return $str;
        }
        $date = new \DateTime($str);
        return $date->setTimezone(new \DateTimeZone('UTC'))->format('Y-m-d');
    }

    /**
     * Get the time formatted according to the ISO 8601.
     */
    public static function getIsoUtcTime($str) {
        if (empty($str)) {
            return $str;
        }
        $date = new \DateTime($str);
        return $date->setTimezone(new \DateTimeZone('UTC'))->format('H:i:sP');
    }

    /**
     * Get the date and time formatted according to the ISO 8601.
     */
    public static function getIsoUtcDatetime($str) {
        if (empty($str)) {
            return $str;
        }
        return self::getIsoUtcDate($str) . 'T' . self::getIsoUtcTime($str);
    }
}