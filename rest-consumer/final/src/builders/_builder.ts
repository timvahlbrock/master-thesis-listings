import { Matcher, RulesMatcher } from "@pact-foundation/pact/src/v3/matchers";

export type Template<C> = {
  [key in keyof C]-?: TemplateEntry<C[key]>;
};

export type TemplateEntry<C> = C extends Date
  ? Matcher<string>
  : C extends Array<infer A>
    ?
        | Matcher<Template<A>[]>
        | Template<A>[]
        | Matcher<Matcher<string>[]>
        | (A extends object ? Matcher<RulesMatcher<A[keyof A]>[]> : never)
    : C extends object
      ? Template<C> | Matcher<Template<C>>
      :
          | Matcher<C>
          | Matcher<string>
          | (C extends boolean ? Matcher<boolean> : never);

export const builderMethods = ["with"];

type BuilderFunction<C> = <K extends keyof C>(
  key: K,
  value: TemplateEntry<C[K]>,
) => Builder<C>;
export type Builder<C> = Template<C> & {
  with: BuilderFunction<C>;
};

export function createBuilderFrom<C>(template: Template<C>): Builder<C> {
  const builderFunction: BuilderFunction<C> = (key, value) =>
    createBuilderFrom({
      ...template,
      [key]: value,
    } as Template<C>);

  return {
    ...template,
    with: builderFunction,
  };
}
