import {
  isMatcher,
  Matcher,
  regex,
} from "@pact-foundation/pact/src/v3/matchers";
import {
  ISO8601_DATE_FORMAT,
  ISO8601_DATETIME_FORMAT,
  ISO8601_DATETIME_WITH_MILLIS_FORMAT,
} from "@pact-foundation/pact/src/dsl/matchers";
import { V3RegexMatcher } from "@pact-foundation/pact/src/v3/types";
import { Builder, builderMethods, TemplateEntry } from "../builders/_builder";

// pact DOES have a date matcher, however it behaves unexpectedly, as it does not respect the example handed, see https://github.com/pact-foundation/pact-js/issues/1076
export const date = (example = "2024-05-10") => ({
  ...verifiedRegex(ISO8601_DATE_FORMAT, example),
  "ifp:metaType": "date",
});
export const datetime = (example = "2024-04-23T02:04:40.181251Z") => ({
  ...verifiedRegex(
    `(${ISO8601_DATETIME_FORMAT}|${ISO8601_DATETIME_WITH_MILLIS_FORMAT})`,
    example,
  ),
  "ifp:metaType": "datetime",
});

function oneOf(
  values: Array<string | number | boolean>,
  example: string | number | boolean,
): V3RegexMatcher {
  return verifiedRegex(`(${values.join("|")})`, example + "");
}

export function likeEnum<C extends string | number | boolean>(
  theEnum: Record<string, C>,
  example?: C,
): V3RegexMatcher {
  return oneOf(Object.values(theEnum), example ?? Object.values(theEnum)[0]);
}

export function verifiedRegex(regexPattern: string | RegExp, example: string) {
  if (example.match(regexPattern) == null) {
    throw new Error(
      `Example value '${example}' does not match target regular expression ${regexPattern}.`,
    );
  }
  return regex(regexPattern, example);
}

export function reify<C>(input: Builder<C>): C;
export function reify<C>(input: Matcher<TemplateEntry<C>>): C;
export function reify<C>(input: Matcher<Builder<C>[]>): C[];
export function reify<C>(input: Matcher<C>): C;
export function reify<C>(input: TemplateEntry<Builder<C>>): C;
export function reify<C>(input: TemplateEntry<C>): C;
export function reify(input: any): any {
  if (isMatcher(input)) {
    if (
      input["pact:matcher:type"] == "regex" &&
      typeof input.value === "string" &&
      ["date", "datetime"].includes(
        input["ifp:metaType" as keyof typeof input] as string,
      )
    ) {
      return new Date(input.value);
    }
    return reify(input.value as Matcher<any>);
  }

  if (Array.isArray(input)) {
    return input.map(reify);
  }

  if (typeof input === "object") {
    if (input === null) {
      return input;
    }
    return Object.keys(input ?? {})
      .filter((propName) => !builderMethods.includes(propName))
      .reduce(
        (acc, propName) => ({
          ...acc,
          [propName]: reify(input[propName as keyof typeof input]),
        }),
        {},
      );
  }

  if (
    typeof input === "number" ||
    typeof input === "string" ||
    typeof input === "boolean" ||
    typeof input === "undefined"
  ) {
    return input;
  }
  throw new Error(`Unable to strip matcher from a '${typeof input}'.`);
}
