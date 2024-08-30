package org.example.restprovider.model

import java.util.*

class GroupReferenceDto (
	val id: UUID,
	val isAdmin: Boolean
) {

	/**
	 * getter is needed because otherwise it 'isAdmin' would be serialized to 'admin'
	 */
	fun getIsAdmin(): Boolean {
		return isAdmin
	}
}