export class Configuration {
	public readonly basePath: string;

	constructor(overrides: {
		basePath: string
	}) {
		this.basePath = overrides.basePath
	}
}