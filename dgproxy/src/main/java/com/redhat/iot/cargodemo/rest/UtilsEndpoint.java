package com.redhat.iot.cargodemo.rest;/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.redhat.iot.cargodemo.model.*;
import com.redhat.iot.cargodemo.service.DGService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.*;

/**
 * A simple REST service which proxies requests to a local datagrid.
 *
 * @author jfalkner@redhat.com
 */

@Path("/utils")
@Singleton
public class UtilsEndpoint {

    public static final int MAX_VEHICLES = 3;
    public static final int MAX_PACKAGES_PER_VEHICLE = 3;
    public static final long DAY_IN_MS = 24*60*60*1000;

    @Inject
    DGService dgService;

    @GET
    @Path("/health")
    public String health() {
        return "ok";
    }

    @POST
    @Path("/resetAll")
    public void resetAll() {

        Map<String, Vehicle> vehiclesCache = dgService.getVehicles();
        Map<String, Customer> customerCache = dgService.getCustomers();
        Map<String, Facility> facilitiesCache = dgService.getFacilities();
        Map<String, Operator> operatorCache = dgService.getOperators();
        Map<String, Shipment> shipmentCache = dgService.getShipments();


        facilitiesCache.clear();
        vehiclesCache.clear();
        customerCache.clear();
        operatorCache.clear();
        shipmentCache.clear();

        for (String COMPANY : COMPANIES) {
            customerCache.put(COMPANY,
                    new Customer(COMPANY, "password"));
        }

        for (String oper : OPERATOR_NAMES){
            operatorCache.put(oper, new Operator(oper, "password"));
        }

        List<String> facs = new ArrayList<String>();
        facs.addAll(Arrays.asList(ORIGINS));
        facs.addAll(Arrays.asList(DESTS));

        for (String facName : facs) {

            facilitiesCache.put(facName,
                    new Facility(facName, facName,
                            new LatLng(-80, 20),
                            Math.random() * 1000.0));

        }


        for (int i = 1; i <= MAX_VEHICLES; i++) {

            String vin = "truck-" + i;

            Vehicle v = new Vehicle(vin, rand(VEHICLE_TYPES));

            Facility v_origin = facilitiesCache.get(rand(ORIGINS));
            Facility v_dest = facilitiesCache.get(rand(DESTS));

            v.setOrigin(v_origin);
            v.setDestination(v_dest);

            List<Telemetry> vehicleTelemetry = new ArrayList<>();
            vehicleTelemetry.add(new Telemetry("°C", 300, 0.0, "Engine Temp", "temp"));
            vehicleTelemetry.add(new Telemetry("rpm", 3500, 0.0, "RPM", "rpm"));
            vehicleTelemetry.add(new Telemetry("psi", 2000.0, 1000.0, "Oil Pressure", "oilpress"));
            v.setTelemetry(vehicleTelemetry);

            Date v_eta = new Date(new Date().getTime() + DAY_IN_MS + (long)(Math.random() * DAY_IN_MS * 2));

            v.setEta(v_eta);
            vehiclesCache.put(vin, v);

            for (int j = 1; j <= MAX_PACKAGES_PER_VEHICLE; j++) {

                List<Facility> route = new ArrayList<Facility>();

                Facility p_origin = facilitiesCache.get(rand(ORIGINS));
                Facility p_dest = facilitiesCache.get(rand(DESTS));

                route.add(p_origin);
                route.add(v_dest);
                route.add(p_dest);

                List<Telemetry> telemetry = new ArrayList<>();
                telemetry.add(new Telemetry("°C", 100.0, 0.0, "Temperature", "Ambient"));
                telemetry.add(new Telemetry("%", 100.0, 0.0, "Humidity", "Humidity"));
                telemetry.add(new Telemetry("lm", 2000.0, 1000.0, "Light", "Light"));
                telemetry.add(new Telemetry("inHg", 200, 100, "Pressure", "Pressure"));

                Customer cust = customerCache.get(rand(COMPANIES));

                // left ~3 days, ago eta ~5 days from now
                Date etd = new Date(new Date().getTime() - DAY_IN_MS - (long)(Math.random() * DAY_IN_MS * 3));
                Date eta = new Date(new Date().getTime() + DAY_IN_MS + (long)(Math.random() * DAY_IN_MS * 4));

                String sensorId = "pkg-" + j;

                Shipment s = new Shipment(customerCache.get(rand(COMPANIES)),
                        "Package " + j, rand(PKG_DESCS),
                        sensorId, route, etd, eta, Math.random() * 2000, v);

                s.setTelemetry(telemetry);
                shipmentCache.put(sensorId + "/" + vin, s);
                System.out.println("Inserting shipment: " + s);
            }

            // add hardwired sensortag
            List<Facility> route = new ArrayList<Facility>();

            Facility p_origin = facilitiesCache.get(rand(ORIGINS));
            Facility p_dest = facilitiesCache.get(rand(DESTS));

            route.add(p_origin);
            route.add(v_dest);
            route.add(p_dest);

            List<Telemetry> telemetry = new ArrayList<>();
            telemetry.add(new Telemetry("°C", 100.0, 0.0, "Temperature", "Ambient"));
            telemetry.add(new Telemetry("%", 100.0, 0.0, "Humidity", "Humidity"));
            telemetry.add(new Telemetry("lm", 2000.0, 1000.0, "Light", "Light"));
            telemetry.add(new Telemetry("inHg", 200, 100, "Pressure", "Pressure"));

            Customer cust = customerCache.get(rand(COMPANIES));

            // left ~3 days, ago eta ~5 days from now
            Date etd = new Date(new Date().getTime() - DAY_IN_MS - (long)(Math.random() * DAY_IN_MS * 3));
            Date eta = new Date(new Date().getTime() + DAY_IN_MS + (long)(Math.random() * DAY_IN_MS * 4));

            String sensorId = "34:B1:F7:D1:44:15";

            Shipment s = new Shipment(customerCache.get(rand(COMPANIES)),
                    "Package sensortag", "34:B1:F7:D1:44:15" + rand(PKG_DESCS),
                    sensorId, route, etd, eta, Math.random() * 2000, v);

            s.setTelemetry(telemetry);
            shipmentCache.put(sensorId + "/" + vin, s);
            System.out.println("Inserting shipment: " + s);


        }
        calcUtilization();

    }

    private String rand(String[] strs) {
        return strs[(int)Math.floor(Math.random() * strs.length)];
    }

    private void calcUtilization() {
        Map<String, Facility> facCache = dgService.getFacilities();
        Map<String, Shipment> shipCache = dgService.getShipments();

        Map<String, Integer> facCount = new HashMap<>();

        int total = 0;

        for (String s1 : shipCache.keySet()) {
            Shipment s = shipCache.get(s1);
            for (Facility f : s.getRoute()) {
                total++;
                if (facCount.containsKey(f.getName())) {
                    facCount.put(f.getName(), facCount.get(f.getName()) + 1);
                } else {
                    facCount.put(f.getName(), 1);
                }
            }
        }

        for (String s1 : facCache.keySet()) {
            Facility f = facCache.get(s1);
            if (!facCount.containsKey(f.getName())) {
                f.setUtilization(0);
            } else {
                f.setUtilization(2.5 * ((double) facCount.get(f.getName()) / (double) total));
            }
            facCache.put(f.getName(), f);
        }
    }

    @PUT
    @Path("/{id}")
    public void put(@PathParam("id") String id, Vehicle value) {
        dgService.getVehicles().put(id, value);
    }

    @GET
    @Path("/summaries")
    @Produces({"application/json"})
    public List<Summary> getSummaries() {

        List<Summary> result = new ArrayList<>();

        Summary vehicleSummary = getVehicleSummary();
        Summary clientSummary = getClientSUmmary();
        Summary packageSummary = getPackageSummary();
        Summary facilitySummary = getFacilitySummary();
        Summary operatorSummary = getOperatorSummary();

        result.add(clientSummary);
        result.add(packageSummary);
        result.add(vehicleSummary);
        result.add(operatorSummary);
        result.add(facilitySummary);

        Summary mgrs = new Summary();
        mgrs.setName("fake");
        mgrs.setTitle("Managers");
        mgrs.setCount(23);
        mgrs.setWarningCount(4);
        mgrs.setErrorCount(1);
        result.add(mgrs);
        return result;
    }

    private Summary getOperatorSummary() {
        Map<String, Operator> cache = dgService.getOperators();

        Summary summary = new Summary();
        summary.setName("operators");
        summary.setTitle("Operators");
        summary.setCount(cache.keySet().size());

        return summary;

    }

    private Summary getFacilitySummary() {
        Map<String, Facility> cache = dgService.getFacilities();

        Summary summary = new Summary();
        summary.setName("facilities");
        summary.setTitle("Facilities");
        summary.setCount(cache.keySet().size());

        long warningCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getUtilization() < .7 && v.getUtilization() > .5)
                .count();

        long errorCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getUtilization() < .5)
                .count();

        summary.setWarningCount(warningCount);
        summary.setErrorCount(errorCount);

        return summary;
    }

    private Summary getPackageSummary() {
        Map<String, Shipment> cache = dgService.getShipments();

        Summary summary = new Summary();
        summary.setName("packages");
        summary.setTitle("Packages");
        summary.setCount(cache.keySet().size());


        long warningCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getStatus() == Shipment.Status.WARNING)
                .count();

        long errorCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getStatus() == Shipment.Status.ERROR)
                .count();

        summary.setWarningCount(warningCount);
        summary.setErrorCount(errorCount);
        return summary;

    }

    private Summary getClientSUmmary() {
        Map<String, Customer> cache = dgService.getCustomers();

        Summary summary = new Summary();
        summary.setName("clients");
        summary.setTitle("Clients");
        summary.setCount(cache.keySet().size());
        return summary;

    }

    private Summary getVehicleSummary() {
        Map<String, Vehicle> cache = dgService.getVehicles();

        Summary summary = new Summary();
        summary.setName("vehicles");
        summary.setTitle("Vehicles");
        summary.setCount(cache.keySet().size());


        long warningCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getStatus() == Vehicle.Status.WARNING)
                .count();

        long errorCount = cache.keySet().stream()
                .map(cache::get)
                .filter(v -> v.getStatus() == Vehicle.Status.ERROR)
                .count();

        summary.setWarningCount(warningCount);
        summary.setErrorCount(errorCount);
        return summary;
    }


    public static final String[] COMPANIES = new String[]{
            "Wonka Industries",
            "Acme Corp",
            "Stark Industries",
            "Ollivander's Wand Shop",
            "Gekko & Co",
            "Wayne Enterprises",
            "Cyberdyne Systems",
            "Cheers",
            "Genco Pura",
            "NY Enquirer",
            "Duff Beer",
            "Bubba Gump Shrimp Co",
            "Olivia Pope & Associates",
            "Sterling Cooper",
            "Soylent",
            "Hooli",
            "Good Burger"
    };

    public static final String[] ORIGINS = new String[]{
            "Winter Springs, FL",
            "Raleigh, NC",
            "Westford, MA",
            "Atlanta, GA",
            "Charleston, SC",
            "Tarboro, NC",
            "Huntsville, AL",
            "Knoxville, TN",
            "Showshoe, WV",
            "Washington, D.C.",
            "Virginia Beach, VA",
            "New York, NY",
            "Jacksonvilla, FL"
    };

    public static final String[] DESTS = new String[]{
            "Chatanooga, TN",
            "Louisville, KY",
            "Omaha, NE",
            "Chicago, IL",
            "Des Moines, IA",
            "Lexington, KY",
            "New Orleans, LA",
            "Mobile, AL"
    };

    public static final String[] VEHICLE_TYPES = new String[] {

            "Box truck",
            "Van",
            "Cutaway van chassis",
            "Medium Duty Truck such as Ford F-650 in North America",
            "Medium Standard Truck",
            "Platform truck",
            "Flatbed truck (may also be light duty trucks)",
            "Firetruck (may also be a heavy truck)",
            "Recreational Vehicle or Motorhome",
            "Concrete transport truck (cement mixer)",
            "Mobile crane",
            "Dump truck",
            "Garbage truck",
            "Log carrier",
            "Refrigerator truck",
            "Tractor unit",
            "Tank truck",
            "Heavy Hauler",
            "F-35"
    };

    public static final String[] PKG_DESCS = new String[] {
            "Spare F-22 Parts",
            "Violins",
            "Antique Baseballs",
            "Frozen Cells",
            "Machined Parts",
            "Misc. Assembly Fasteners",
            "Fresh Fruit",
            "Frozen Steaks",
            "Precious Jewels",
            "Optical Hard Drives",
            "Polyjuice Potion",
            "Live Bait"
    };

    public static final String[] OPERATOR_NAMES = new String[]{
            "R. Kint",
            "H. Potter",
            "A. Ventura",
            "H. Lime",
            "S. Kowalski",
            "D. Vader",
            "S. Spade",
            "D. Strangelove",
            "T. Montana",
            "N. Rae",
            "J. Benjamin",
            "A. DeLarge",
            "J. Cousteau",
            "E. Scissorhands",
            "G. Bailey",
            "Lt. Kilgore",
            "T. Dude",
            "F. Booth",
            "F. Kreuger"
    };

}

