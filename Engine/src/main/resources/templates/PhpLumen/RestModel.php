<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class RestModel extends Model {

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
}