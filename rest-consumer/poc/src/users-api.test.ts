import {
	UsersApi,
	User
} from "./users-api";
import { PactV4 } from "@pact-foundation/pact";
import path from "path";
import { HTTPMethods } from "@pact-foundation/pact/src/common/request";
import { Configuration } from "./configuration";

const sampleUser: User = {
	id: "9a9845c4-8a46-4bf9-89a5-ecd3c3b66479",
	firstName: "Jane",
	lastName: "Appleseed",
	groups: [{
		id: "1f2a451b-843c-4e6a-904d-fbee06dc61d7",
		isAdmin: false,
	}]
};

describe("users api", () => {
	const pact = new PactV4({
		consumer: "GUI",
		provider: "User Service",
		dir: path.resolve(process.cwd(), "pacts"),
	});

	it("handles a request for a user", () => {
		return pact
			.addInteraction()
			.given("there is a user")
			.uponReceiving("a request for the user")
			.withRequest(
				HTTPMethods.GET,
				`/v1/users/${sampleUser.id}`,
			)
			.willRespondWith(
				200,
				(b) => b.jsonBody(sampleUser),
			)
			.executeTest(async (mockServer) => {
				const configuration = new Configuration({
					basePath: mockServer.url,
				});
				const api = new UsersApi(configuration);

				const result = await api.getUser({id: sampleUser.id});

				expect(result).toEqual(sampleUser);
			});
	});
});
