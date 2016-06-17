<?php

namespace App\Models;
{{#if isUser}}
use Illuminate\Auth\Authenticatable;
use Laravel\Lumen\Auth\Authorizable;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Contracts\Auth\Authenticatable as AuthenticatableContract;
use Illuminate\Contracts\Auth\Access\Authorizable as AuthorizableContract;{{/if}}

class {{resourceClass}} extends RestModel{{#if isUser}} implements AuthenticatableContract, AuthorizableContract{{/if}} {
    {{#if isUser}}use Authenticatable, Authorizable;
    {{/if}}
    protected $table = '{{resourceTable}}';

    protected $fillable = [
        {{#each belongsToRelations}}'{{fk}}', {{/each}}{{#each fillableAttributes}}'{{this}}', {{/each}}
    ];

    protected $hidden = [
        {{#each hiddenAttributes}}'{{this}}', {{/each}}
    ];

    protected static $filterable = [
        {{#each belongsToRelations}}'{{fk}}', {{/each}}{{#each filterableAttributes}}'{{this}}', {{/each}}
    ];

    protected static $sortable = [
        {{#each belongsToRelations}}'{{fk}}', {{/each}}{{#each sortableAttributes}}'{{this}}', {{/each}}
    ];

    protected $casts = [
        {{#each belongsToRelations}}'{{fk}}' => 'integer',
        {{/each}}{{#each casts}}'{{prop}}' => '{{type}}',{{#unless @last}}{{! Skips last new line }}
        {{/unless}}{{/each}}
    ];

    protected static $validationRules = [
        {{#each belongsToRelations}}'{{fk}}' => 'integer|min:1',{{! Should be 'required' sometimes, but in any case the action will be blocked, just that the error message will not be that accurate }}
        {{/each}}{{#each validationRules}}'{{prop}}' => [{{{rule}}}],{{#unless @last}}
        {{/unless}}{{/each}}
    ];

    protected $dates = [
        // {{#each dateAttributes}}'{{name}}', {{/each}}{{#each dateTimeAttributes}}'{{name}}', {{/each}}
    ];

    protected $appends = [
        {{#each dateAttributes}}'{{name}}', {{/each}}{{#each timeAttributes}}'{{name}}', {{/each}}{{#each dateTimeAttributes}}'{{name}}', {{/each}}
    ];
    {{#each dateAttributes}}
    public function get{{camelName}}Attribute($value) {
        return self::getIsoUtcDate($this->attributes['{{name}}']);
    }

    public function set{{camelName}}Attribute($value) {
        $this->attributes['{{name}}'] = self::getIsoUtcDate($value);
    }
    {{/each}}
    {{#each timeAttributes}}
    public function get{{camelName}}Attribute($value) {
        return self::getIsoUtcTime($this->attributes['{{name}}']);
    }

    public function set{{camelName}}Attribute($value) {
        $this->attributes['{{name}}'] = self::getIsoUtcTime($value);
    }
    {{/each}}
    {{#each dateTimeAttributes}}
    public function get{{camelName}}Attribute($value) {
        return self::getIsoUtcDatetime($this->attributes['{{name}}']);
    }

    public function set{{camelName}}Attribute($value) {
        $this->attributes['{{name}}'] = self::getIsoUtcDatetime($value);
    }
    {{/each}}
    {{#each encryptedAttributes}}
    public function set{{camelName}}Attribute($value) {
        $this->attributes['{{name}}'] = \App\Utils\Security::hash($value);
    }
    {{/each}}
    {{#each fileAttributes}}
    public function get{{camelName}}Attribute($value, $fmt = 'raw') {
        if (empty($value)) {
            return '';
        }

        switch ($fmt) {
        case 'b64':
        case 'base64':
            return base64_encode($value);
        case 'raw':
        default:
            return '/{{resourceRoute}}/' . $this->attributes['id'] . $this->fileUrlPart . '/{{name}}';
        }
    }
    {{/each}}
    {{#each hasOneRelations}}public function {{fn}}() {
        return $this->hasOne('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each hasManyRelations}}public function {{fn}}() {
        return $this->hasMany('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each belongsToRelations}}public function {{fn}}() {
        return $this->belongsTo('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each belongsToManyRelations}}public function {{fn}}() {
        return $this->belongsToMany('App\Models{{classBack}}', '{{middleTable}}', '{{fk}}', '{{id}}'){{#if pivotCols}}->withPivot({{#each pivotCols}}'{{this}}'{{#unless @last}}, {{/unless}}{{/each}}){{/if}};
    }
    {{/each}}
}