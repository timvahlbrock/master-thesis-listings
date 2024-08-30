import {Configuration} from "./configuration";

export interface User {
	id: string,
	firstName: string,
	lastName: string,
	groups: Array<{
		id: string,
		isAdmin: boolean
	}>
}

export class UsersApi {
	constructor(
		private readonly configuration: Configuration
	) {
	}

	public async getUser(params: { id: string }): Promise<User> {
		const response = await fetch(this.configuration.basePath + "/v1/users/" + params.id)
		return response.json()
	}
}