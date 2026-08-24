package org.webfunction.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webfunction.Client;
import org.webfunction.Options;

/**
 * Fetches the Web Function package at https://api.reservepay.com/merchants
 * and calls its "available-api-versions" endpoint, printing the result.
 *
 * Requires a bearer token - set RESERVEPAY_BEARER_TOKEN before running,
 * rather than hardcoding it here.
 *
 * Run from the project root with:
 *
 *   RESERVEPAY_BEARER_TOKEN=... mvn -q compile exec:java \
 *       -Dexec.mainClass="org.webfunction.examples.AvailableApiVersions"
 */
public final class AvailableApiVersions {

    private static final String PACKAGE_URL = "https://api.reservepay.com/merchants";

    public static void main(String[] args) {
        String token = System.getenv("RESERVEPAY_BEARER_TOKEN");
        if (token == null || token.isEmpty()) {
            System.err.println("RESERVEPAY_BEARER_TOKEN is not set");
            System.exit(1);
        }

        Client client;
        try {
            client = Client.fromPackageEndpoint(PACKAGE_URL, new Options().bearerAuth(token));
        } catch (RuntimeException e) {
            System.err.println("fetching package: " + e.getMessage());
            System.exit(1);
            return;
        }

        // No named methods are generated yet (e.g. client.availableApiVersions()) -
        // that's the future wfn CLI `java` codegen target's job. For now every
        // call goes through the explicit call(name, args), same as Go's
        // client.Call("available-api-versions", nil).
        Object result;
        try {
            result = client.call("available-api-versions", null);
        } catch (RuntimeException e) {
            System.err.println("calling available-api-versions: " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            ObjectMapper printer = new ObjectMapper();
            System.out.println(printer.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (Exception e) {
            System.err.println("encoding result: " + e.getMessage());
            System.exit(1);
        }
    }
}