import { LogLevel, PactV4, V3MockServer } from "@pact-foundation/pact";
import { SpecificationVersion } from "@pact-foundation/pact/src/v3/matchers";
import { Configuration } from "../configuration";

const pact = new PactV4({
	consumer: "GUI",
	provider: "User Service",
	spec: SpecificationVersion.SPECIFICATION_VERSION_V4,
	logLevel: (process.env.LOG_LEVEL as LogLevel) || "warn",
});

interface DescribePact {
	(pathName: string, suite: ContractSuite): void;

	only: (pathName: string, suite: ContractSuite) => void;
	skip: (pathName: string, suite: ContractSuite) => void;
}

export const describePact: DescribePact = (
	pathName: string,
	suite: ContractSuite,
) => {
	describe(pathName, () => suite(pact));
};
describePact.only = (pathName: string, suite: ContractSuite) => {
	describe.only(pathName, () => suite(pact));
};
describePact.skip = (pathName: string, suite: ContractSuite) => {
	describe.skip(pathName, () => suite(pact));
};

export type ContractSuite = (pact: PactV4) => void;

export function withApi<C>(
	api: new (config: Configuration) => C,
	test: (api: C) => Promise<void>,
): (mockServer: V3MockServer) => Promise<void> {
	return (mockServer) => test(create(api, mockServer.url));
}

export function create<C>(
	api: new (config: Configuration) => C,
	url: string,
): C {
	return new api(
		new Configuration({
			basePath: url,
		}),
	);
}
