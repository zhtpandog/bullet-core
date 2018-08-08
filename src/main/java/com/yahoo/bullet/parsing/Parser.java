/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yahoo.bullet.common.BulletConfig;

public class Parser {
    private static final FieldTypeAdapterFactory<Clause> CLAUSE_FACTORY =
            FieldTypeAdapterFactory.of(Clause.class)
                                   .registerSubType(ObjectFilterClause.class, Parser::isObjectFilterClause)
                                   .registerSubType(StringFilterClause.class, Parser::isStringFilterClause)
                                   .registerSubType(LogicalClause.class, Parser::isLogicalClause);
    private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(CLAUSE_FACTORY)
                                                      .excludeFieldsWithoutExposeAnnotation()
                                                      .create();

    private static Boolean isFilterClause(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(Clause.OPERATION_FIELD);
        return jsonElement != null && Clause.Operation.RELATIONALS.contains(jsonElement.getAsString());
    }

    private static Boolean isStringFilterClause(JsonObject jsonObject) {
        if (!isFilterClause(jsonObject)) {
            return false;
        }
        JsonArray values = (JsonArray) jsonObject.get(FilterClause.VALUES_FIELD);
        return values != null && values.size() != 0 && values.get(0).isJsonPrimitive();
    }

    private static Boolean isObjectFilterClause(JsonObject jsonObject) {
        if (!isFilterClause(jsonObject)) {
            return false;
        }
        JsonArray values = (JsonArray) jsonObject.get(FilterClause.VALUES_FIELD);
        return values != null && values.size() != 0 && values.get(0).isJsonObject();
    }

    private static Boolean isLogicalClause(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(Clause.OPERATION_FIELD);
        return jsonElement != null && Clause.Operation.LOGICALS.contains(jsonElement.getAsString());
    }

    /**
     * Parses a Query out of the query string.
     *
     * @param queryString The String version of the query.
     * @param config Additional configuration for the query.
     *
     * @return The parsed, configured Query.
     * @throws com.google.gson.JsonParseException if there was an issue parsing the query.
     */
    public static Query parse(String queryString, BulletConfig config) {
        Query query = GSON.fromJson(queryString, Query.class);
        query.configure(config);
        return query;
    }
}

