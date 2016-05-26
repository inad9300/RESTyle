<?php

namespace App\Policies;

/**
 * Class responsible for storing the available roles and helping to check if a certain user
 * is authorized to do something based on its role.
 */
class Role {

	private static $roles = [
		{{#each roles}}[ 'name' => '{{name}}'{{#if isA}}, 'isA' => '{{isA}}'{{/if}}{{#if isAdmin}}, 'isAdmin' => true{{/if}}{{#if isGuest}}, 'isGuest' => true{{/if}} ],
		{{/each}}
	];

	private static function getRoleByName($roleName) {
		foreach (self::$roles as $role) {
			if ($role['name'] === $roleName) {
				return $role;
			}
		}
		return null;
	}

	/**
	 * Returns an array with the roles the given one inherits from, recursively.
	 */
	private static function getChildren($roleName) {
		$role = self::getRoleByName($roleName);

		if (!$role || !isset($role['isA']) || !$role['isA']) {
			return [];
		}
		return array_merge([$role['isA']], self::getChildren($role['isA']));
	}

	/**
	 * Determines whether the user has a role which is over (has equal or more priviledges)
	 * than the given one.
	 */
	public static function isOver($user, $roleName) {

		// Accept an array of role names too
		if (is_array($roleName)) {
			foreach ($roleName as $role) {
				if (self::isOver($user, $role)) {
					return true;
				}
			}
			return false;
		}

		{{#if adminName}}// Admin is over everybody
		if ($user->role === '{{adminName}}') {
			return true;
		}
		{{/if}}
		{{#if guestName}}// Everybody is over guest
		if ($roleName === '{{guestName}}') {
			return true;
		}
		{{/if}}
		// If there is a direct match
		if ($user->role === $roleName) {
			return true;
		}

		// If the given role is one of the children of the user's role
		return in_array(
			$roleName, self::getChildren($user->role)
		);
	}
}