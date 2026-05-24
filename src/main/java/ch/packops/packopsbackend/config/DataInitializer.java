package ch.packops.packopsbackend.config;

import ch.packops.packopsbackend.domain.Category;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.security.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final CategoryRepository categoryRepository;
    private final ProductConfigurationRepository productConfigurationRepository;
    private final ConfigurationRepository configurationRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordService passwordService,
                           CategoryRepository categoryRepository,
                           ProductConfigurationRepository productConfigurationRepository,
                           ConfigurationRepository configurationRepository) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.categoryRepository = categoryRepository;
        this.productConfigurationRepository = productConfigurationRepository;
        this.configurationRepository = configurationRepository;

    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@packops.ch");
        admin.setPasswordHash(passwordService.hash("admin123"));
        admin.setRole("admin");
        admin.setLanguage("de");

        userRepository.save(admin);
        generateProducts();
    }

        private void generateProducts() {

        if (productConfigurationRepository.count() > 0) {

            return;

        }


// 1. Create Categories

        Category breakfastCat = new Category(); breakfastCat.setName("Frühstück"); categoryRepository.save(breakfastCat);

        Category pastaCat = new Category(); pastaCat.setName("Pasta"); categoryRepository.save(pastaCat);

        Category snacksCat = new Category(); snacksCat.setName("Snacks"); categoryRepository.save(snacksCat);

        Category bakingCat = new Category(); bakingCat.setName("Backwaren"); categoryRepository.save(bakingCat);

        Category sweetCat = new Category(); sweetCat.setName("Süssungsmittel"); categoryRepository.save(sweetCat);

        Category spiceCat = new Category(); spiceCat.setName("Gewürze"); categoryRepository.save(spiceCat);

        Category teaCat = new Category(); teaCat.setName("Tee"); categoryRepository.save(teaCat);

        Category legumeCat = new Category(); legumeCat.setName("Hülsenfrüchte"); categoryRepository.save(legumeCat);

        Category sweetsCat = new Category(); sweetsCat.setName("Süssigkeiten"); categoryRepository.save(sweetsCat);


// 2. Create Products


// Frühstück

        createProduct("Müsli Mix", "Hafer, Nüsse und Trockenfrüchte", 375, 8, "🥣", "from-yellow-500 to-amber-600", breakfastCat);

        createProduct("Haferflocken", "Kernige Haferflocken", 500, 10, "🌾", "from-yellow-500 to-amber-600", breakfastCat);

        createProduct("Cornflakes", "Knusprige Maisflocken", 400, 8, "🥣", "from-yellow-400 to-orange-500", breakfastCat);


// Pasta

        createProduct("Nudeln Penne", "Italienische Hartweizen Pasta", 500, 10, "🍝", "from-yellow-400 to-orange-500", pastaCat);

        createProduct("Spaghetti", "Lange Hartweizen Spaghetti", 500, 10, "🍝", "from-yellow-400 to-orange-500", pastaCat);

        createProduct("Fusilli", "Spiralförmige Pasta", 500, 10, "🍝", "from-yellow-400 to-orange-500", pastaCat);


// Backwaren

        createProduct("Mehl Type 405", "Weizenmehl für Kuchen und Gebäck", 500, 10, "🌾", "from-amber-300 to-amber-500", bakingCat);

        createProduct("Vollkornmehl", "Dunkles Weizenvollkornmehl", 500, 10, "🌾", "from-amber-600 to-brown-700", bakingCat);

        createProduct("Pinienkerne", "Naturbelassene Pinienkerne", 50, 1, "🌲", "from-amber-300 to-amber-500", bakingCat);


// Süssungsmittel

        createProduct("Zucker Weiss", "Kristallzucker raffiniert", 500, 10, "🧂", "from-slate-100 to-slate-300", sweetCat);

        createProduct("Rohrzucker", "Brauner unraffinierter Rohrzucker", 500, 10, "🧂", "from-amber-600 to-brown-700", sweetCat);


// Gewürze

        createProduct("Salz Meersalz", "Natürliches Meersalz grob", 500, 10, "🧂", "from-blue-200 to-blue-400", spiceCat);

        createProduct("Pfeffer Schwarz", "Ganze schwarze Pfefferkörner", 100, 3, "🧂", "from-slate-400 to-slate-600", spiceCat);

        createProduct("Paprikapulver", "Edelsüsses Paprikapulver", 100, 3, "🌶️", "from-red-400 to-orange-600", spiceCat);

        createProduct("Zimt", "Gemahlener Ceylon Zimt", 50, 2, "🪵", "from-amber-600 to-brown-700", spiceCat);

        createProduct("Currypulver", "Milde Currymischung", 75, 2, "🟡", "from-yellow-500 to-amber-600", spiceCat);

        createProduct("Oregano", "Getrockneter Oregano gerebelt", 30, 1, "🌿", "from-green-600 to-emerald-700", spiceCat);


// Snacks

        createProduct("Nüsse Mix", "Cashew, Mandeln, Walnüsse", 200, 5, "🥜", "from-amber-600 to-brown-700", snacksCat);

        createProduct("Studentenfutter", "Klassische Nuss-Frucht-Mischung", 150, 4, "🥜", "from-amber-600 to-brown-700", snacksCat);

        createProduct("Pistazien", "Geröstete Pistazien mit Salz", 100, 3, "🌰", "from-green-600 to-emerald-700", snacksCat);

        createProduct("Kürbiskerne", "Getrocknete Kürbiskerne", 80, 2, "🎃", "from-green-600 to-emerald-700", snacksCat);

        createProduct("Popcorn", "Gesalzenes Popcorn", 90, 3, "🍿", "from-yellow-400 to-orange-500", snacksCat);

        createProduct("Kartoffelchips", "Knusprige Kartoffelchips", 150, 5, "🥔", "from-yellow-500 to-amber-600", snacksCat);

        createProduct("Salzbrezeln", "Kleine salzige Brezeln", 120, 4, "🥨", "from-amber-500 to-orange-600", snacksCat);

        createProduct("Tortilla Chips", "Maischips mit Käsegeschmack", 200, 5, "🌮", "from-yellow-500 to-amber-600", snacksCat);

        createProduct("Erdnussflips", "Gebackene Erdnussflips", 150, 4, "🥜", "from-amber-500 to-orange-600", snacksCat);


// Süssigkeiten

        createProduct("Jelly Beans", "Bunte Geleebohnen", 150, 5, "🍬", "from-purple-500 to-pink-600", sweetsCat);

        createProduct("Gummibärchen", "Fruchtige Gummibärchen", 200, 5, "🧸", "from-red-400 to-orange-600", sweetsCat);

        createProduct("Schokolinsen", "Bunte Schokolinsen", 125, 3, "🍫", "from-indigo-500 to-blue-600", sweetsCat);

        createProduct("Lakritz", "Süsse Lakritzschnecken", 175, 4, "🖤", "from-slate-400 to-slate-600", sweetsCat);

        createProduct("Marshmallows", "Weiche Schaumzuckerware", 100, 3, "☁️", "from-slate-100 to-slate-300", sweetsCat);

        createProduct("Fruchtgummi", "Saure Apfelringe", 150, 4, "🍏", "from-green-600 to-emerald-700", sweetsCat);

        createProduct("Schokoladentafel", "Vollmilchschokolade", 100, 3, "🍫", "from-amber-600 to-brown-700", sweetsCat);

        createProduct("Karamellbonbons", "Weiche Sahnekaramellbonbons", 150, 4, "🍬", "from-amber-500 to-orange-600", sweetsCat);

        createProduct("Kaugummi", "Pfefferminz Kaugummidragees", 50, 1, "🫧", "from-blue-200 to-blue-400", sweetsCat);


// Tee

        createProduct("Tee Earl Grey", "Schwarztee mit Bergamotte", 100, 3, "🍵", "from-green-600 to-emerald-700", teaCat);


// Hülsenfrüchte

        createProduct("Linsen Rot", "Rote Linsen geschält", 500, 10, "🫘", "from-red-400 to-orange-600", legumeCat);


        System.out.println("Default products created.");

    }


// Helper method to keep code clean

    private void createProduct(String name, String description, int targetWeight,

                               int tolerance, String icon, String color, Category category) {

        ProductConfiguration product = new ProductConfiguration();

        product.setName(name);

        product.setDescription(description);

        product.setDefaultTargetWeight(targetWeight);

        product.setDefaultTolerance(tolerance);

        product.setIcon(icon);

        product.setColor(color);

        product.setActive(true);

        product.setCategory(category);

        productConfigurationRepository.save(product);

    }
}