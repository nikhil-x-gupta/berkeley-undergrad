package bearmaps.test;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import org.junit.Before;
import org.junit.Test;
import bearmaps.proj2c.Router;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Test of the autocomplete for Gold Points*/
public class TestAutocomplete {
    private static final String PATHS_FILE = "../library-sp19/data/proj2c_test_inputs/path_results.txt";
    private static final String RESULTS_FILE = "../library-sp19/data/proj2c_test_inputs/directions_results.txt";
    private static final int NUM_TESTS = 8;
    private static final String OSM_DB_PATH = "../library-sp19/data/proj2c_xml/berkeley-2019.osm.xml";
    private static AugmentedStreetMapGraph graph;

    @Before
    public void setUp() throws Exception {
        graph = new AugmentedStreetMapGraph(OSM_DB_PATH);
    }

    @Test
    public void testAutocompleteWithPrefix() throws Exception {
        String prefix = "a";
        ArrayList<String> actual = (ArrayList<String>) graph.getLocationsByPrefix(prefix);
        System.out.println(actual);
        String expectedStrings = "A16, A2 Cafe, A Cote, A Dora Pie, Abe's Pizza, Abe's Cafe, Accelerator Magnet, Acci Gallery, A'Cuppa Tea, Acupuncture and Integrative Medicine College - Berkeley, Ace Test Only Smog Center, Ace Monster Toys, Acme Bar, Acme Bread Company, Ada's Cafe, Addis Ethiopian, Addison Yoga Loft, Adeline & Essex (Ed Roberts Campus AccessMobile), Adeline Yoga Studio, A.G. Ferrari, Agrodolce, Aiban Market, Ajanta, AJ's Auto Clinic, AKEMI, Aki's, ALPR, Al Lasher's Electronics, Alaska Gas, Alba's Glass, Albatross, Albany, Albany Arts Gallery, Albany Branch Alameda County Library, Albany Twin Theatre, Alropacars Auto Repair, Alcatraz & Telegraph, Alchemy Collective Cafe, Aldo, Alfonso Cafe, Algorithm Coffee, All American Natural Cafe, All Nations Church of Christ, Allston & Jefferson, Allergro Ballroom, Allegro Coffee, Allure, Almare, AM/PM, Amberina's Boutique, AMC Emeryville 16, American Cancer Society Discovery Store, American Eagle Outfitters, American Haircuts, Ana's Flowers & Gifts, Anchalee, Anthropology Library, Anthropologie, Anthony's Auto Collision Repair, Angeline's Louisiana Kitchen, Animal Farm/Wild Bird Annex, Anna Bella Nail, Anna Head School, Anna Head Lot, Anna Yates Elementary School, Annaher Grocery & Liquor, Annie's Oak, Apple Store, Apostolic Lutheran Church, Arabia Salon De Beauté, Arco, Art Thou, Art's Automotive, Arthur Mac's Tap & Snack, Artist At Play Studio And Gallery, Artist and Craftsman, Arizmendi, Arinell Pizza, Arlington Av & Indian Rock Path, Armstrong College Building, As You Wish Frozen Yogurt, ASA Liquors, Asado, Assyrian Cultural Center, Asha Tea House, Ashby, Ashby & Telegraph, Ashby BART, Ashby Flea Market, Ashby Flowers, Asia, Asmara, AT&T, Athleta, Atomic Garden, Au Coquelet, Aura Jewelers, Aurora Theatre, Australasia, Auto Options Tire, Wheel and Auto Service, Aunt Mary's Cafe, Avant Card, Avenida Dr & Queens Rd, Avenida Dr & Campus Dr, Azure Salon";
        String[] expected = expectedStrings.split(", ");
        System.out.println(expected.length);
        System.out.println(actual.size());
        for (String string : expected) {
            if (!actual.contains(string)) {
                System.out.println(string);
            }
        }
    }
}
