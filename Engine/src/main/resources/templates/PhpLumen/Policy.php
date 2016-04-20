<?php

namespace App\Policies;

use App{{userClassBack}};{{#if differentRes}}
use App{{resClassBack}};{{/if}}

class {{resClass}}Policy {

    public function read({{userClass}} $user{{#if differentRes}}, {{resClass}} ${{resVar}}{{/if}}) {
        return Role::isOver($user, [{{#each readRoles}}'{{this}}', {{/each}}]);
    }

    public function create({{userClass}} $user{{#if differentRes}}, {{resClass}} ${{resVar}}{{/if}}) {
        return Role::isOver($user, [{{#each createRoles}}'{{this}}', {{/each}}]);
    }

    public function update({{userClass}} $user{{#if differentRes}}, {{resClass}} ${{resVar}}{{/if}}) {
        return Role::isOver($user, [{{#each updateRoles}}'{{this}}', {{/each}}]);
    }

    public function delete({{userClass}} $user{{#if differentRes}}, {{resClass}} ${{resVar}}{{/if}}) {
        return Role::isOver($user, [{{#each deleteRoles}}'{{this}}', {{/each}}]);
    }
}