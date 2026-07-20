-- Dataset `type` changed from a single string to a list (0..n dataset-types).
UPDATE datasets
SET data = jsonb_set(data, '{type}', to_jsonb(array [data ->> 'type']))
WHERE jsonb_typeof(data -> 'type') = 'string';
