# Master Thesis Listings

This repository contains the long version of the listings used in my master thesis "Evaluation of Contract Testing in a Large Microservice System". It contains a folder for each of the participant types, which itself contains two folders for the proof of concept and final implementation. The included code is not an except from a production system, but was rather written to be able to present the findings from the case study system, without exposing any confidential code or data. Nevertheless are all of the included implementations executable.

## Setup
The REST consumer requires [Node.js](https://nodejs.org/en), the other participants require Java 21 to be installed and the `JAVA_HOME` environment variable to be set. While gradle is used, no own gradle installation is required. If you want to copy the contract from the REST consumer implementations to the corresponding REST provider implementations, bash needs to be installed. The one that comes with Git should be sufficient. To install the REST consumer dependencies, you need to run `npm install` in the corresponding directory. The other participants install dependencies automatically.

## Test Execution

In the REST consumer implementations run `npm test` to run the contract tests. If you modified something on the tests you will want to delete the contracts file from the `pacts` folder beforehand, as Pact will append to the existing file otherwise. To copy the contracts to the REST provider, run `npm run copyContracts`. The proof of concept consumer will copy the contract to the proof of concept provider and the final consumer implementation will copy its contract to the final provider implementation. Alternatively you can copy the contract file to the `src/main/resources/pacts` folder manually.

The test execution in the implementations of the remaining participants can be executed with `./gradlew test`. To copy the contracts from the event consumer implementation to the corresponding event provider implementation, run `./gradlew copyContracts`. Alternatively you can copy the file from `build/pacts` in the consumer implementation to  `src/main/resources/pacts` in the provider implementation manually.

## Archive

This repository is archived at https://archive.softwareheritage.org/browse/origin/directory/?origin_url=https://github.com/timvahlbrock/master-thesis-listings .
