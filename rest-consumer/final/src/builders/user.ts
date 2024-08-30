import { boolean, eachLike, string, uuid } from "@pact-foundation/pact/src/v3/matchers";
import { createBuilderFrom } from "./_builder";
import { User } from "../users-api";

export function aUser() {
	return createBuilderFrom<User>({
		id: uuid("9a9845c4-8a46-4bf9-89a5-ecd3c3b66479"),
		firstName: string(),
		lastName: string(),
		groups: eachLike({
			id: uuid("4aef2a66-d725-4664-a13b-309e7b6096aa"),
			isAdmin: boolean()
		})
	});
}