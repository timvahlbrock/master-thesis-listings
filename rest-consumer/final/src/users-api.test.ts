import { UsersApi } from "./users-api";
import { HTTPMethods } from "@pact-foundation/pact/src/common/request";
import { describePact, withApi } from "./helpers/pact";
import { aUser } from "./builders/user";
import { reify } from "./helpers/ownMatchers";

describePact("users api", (pact) => {
	it("handles a request for a user", () => {
		const id = "9a9845c4-8a46-4bf9-89a5-ecd3c3b66479";
		const sampleUser = aUser();

		return pact
			.addInteraction()
			.given("there is a user", { id: id })
			.uponReceiving("a request for the user")
			.withRequest(
				HTTPMethods.GET,
				`/v1/users/${id}`,
			)
			.willRespondWith(
				200,
				(b) => b.jsonBody(sampleUser),
			)
			.executeTest(
				withApi(UsersApi, async api => {
					const result = await api.getUser({ id: id });

					expect(result).toEqual(reify(sampleUser));
				})
			);
	});
});
