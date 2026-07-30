import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

test("admin environment example routes public API traffic through the gateway", () => {
  const contents = readFileSync(new URL("../.env.example", import.meta.url), "utf8");

  assert.match(contents, /^NEXT_PUBLIC_API_BASE_URL=http:\/\/localhost:8082\/api$/m);
  assert.doesNotMatch(contents, /^NEXT_PUBLIC_API_BASE_URL=$/m);
});
