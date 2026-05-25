package ch.packops.packopsbackend.config;


import ch.packops.packopsbackend.domain.*;
import ch.packops.packopsbackend.repository.*;

import ch.packops.packopsbackend.domain.Category;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.UserRepository;

import ch.packops.packopsbackend.security.PasswordService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final CategoryRepository categoryRepository;
    private final ProductConfigurationRepository productConfigurationRepository;
    private final ConfigurationRepository configurationRepository;

    private final ProductConfigurationTranslationRepository translationRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordService passwordService,
                           CategoryRepository categoryRepository,
                           ProductConfigurationRepository productConfigurationRepository,

                           ConfigurationRepository configurationRepository,
                           ProductConfigurationTranslationRepository translationRepository,
                           CategoryTranslationRepository categoryTranslationRepository) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.categoryRepository = categoryRepository;
        this.productConfigurationRepository = productConfigurationRepository;
        this.configurationRepository = configurationRepository;

        this.categoryTranslationRepository = categoryTranslationRepository;
        this.translationRepository = translationRepository;
    }

    @Override
    public void run(String... args) {
        generateUsers();
        generateProducts();
        // generateConfiguration();
    }

    private void generateConfiguration() {
        if (configurationRepository.count() == 0) {

            Configuration configuration = new Configuration();

            configuration.setTargetWeight(250);
            configuration.setTolerance(5);
            configuration.setMaxUnits(100);
            configuration.setMaxIterations(3);
            configuration.setLanguage("de");
            configuration.setUpdatedAt(LocalDateTime.now());

            configurationRepository.save(configuration);
        }
    }

    private void generateUsers() {
        // Admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordService.hash("admin123"));
            admin.setEmail("admin@packops.ch");
            admin.setRole("admin");
            admin.setLanguage("de");
            userRepository.save(admin);
            System.out.println("Default admin user created.");
        }
    }

     // Kleiner Hilfs-Record für die saubere Übergabe der Übersetzungen
    public record Translation(String lang, String name, String desc) {}

    @Transactional
    protected void generateProducts() {
        if (productConfigurationRepository.count() > 0) {
            return;
        }

        // 1. Create Categories
        Category breakfastCat = createCategoryWithTranslations("Frühstück", "Petit-déjeuner", "Breakfast");
        Category pastaCat = createCategoryWithTranslations("Pasta", "Pâtes", "Pasta");
        Category snacksCat = createCategoryWithTranslations("Snacks", "Snacks", "Snacks");
        Category bakingCat = createCategoryWithTranslations("Backwaren", "Pâtisseries", "Baked Goods");
        Category sweetCat = createCategoryWithTranslations("Süssungsmittel", "Édulcorants", "Sweeteners");
        Category spiceCat = createCategoryWithTranslations("Gewürze", "Épices", "Spices");
        Category teaCat = createCategoryWithTranslations("Tee", "Thé", "Tea");
        Category legumeCat = createCategoryWithTranslations("Hülsenfrüchte", "Légumineuses", "Legumes");
        Category sweetsCat = createCategoryWithTranslations("Süssigkeiten", "Friandises", "Sweets");

        // 2. Create Products mit i18n

        // Frühstück
        createProduct("🥣", "from-yellow-500 to-amber-600", 375, 8, breakfastCat,
                new Translation("de", "Müsli Mix", "Hafer, Nüsse und Trockenfrüchte"),
                new Translation("fr", "Mélange Muesli", "Avoine, noix et fruits secs"),
                new Translation("en", "Muesli Mix", "Oats, nuts and dried fruit")
        );
        createProduct("🌾", "from-yellow-500 to-amber-600", 500, 10, breakfastCat,
                new Translation("de", "Haferflocken", "Kernige Haferflocken"),
                new Translation("fr", "Flocons d'avoine", "Flocons d'avoine complets"),
                new Translation("en", "Oat Flakes", "Hearty oat flakes")
        );
        createProduct("🥣", "from-yellow-400 to-orange-500", 400, 8, breakfastCat,
                new Translation("de", "Cornflakes", "Knusprige Maisflocken"),
                new Translation("fr", "Cornflakes", "Flocons de maïs croustillants"),
                new Translation("en", "Cornflakes", "Crunchy corn flakes")
        );

        // Pasta
        createProduct("🍝", "from-yellow-400 to-orange-500", 500, 10, pastaCat,
                new Translation("de", "Nudeln Penne", "Italienische Hartweizen Pasta"),
                new Translation("fr", "Penne", "Pâtes italiennes de blé dur"),
                new Translation("en", "Penne Pasta", "Italian durum wheat pasta")
        );
        createProduct("🍝", "from-yellow-400 to-orange-500", 500, 10, pastaCat,
                new Translation("de", "Spaghetti", "Lange Hartweizen Spaghetti"),
                new Translation("fr", "Spaghetti", "Longues pâtes de blé dur"),
                new Translation("en", "Spaghetti", "Long durum wheat spaghetti")
        );
        createProduct("🍝", "from-yellow-400 to-orange-500", 500, 10, pastaCat,
                new Translation("de", "Fusilli", "Spiralförmige Pasta"),
                new Translation("fr", "Fusilli", "Pâtes en spirale"),
                new Translation("en", "Fusilli", "Spiral-shaped pasta")
        );

        // Backwaren
        createProduct("🌾", "from-amber-300 to-amber-500", 500, 10, bakingCat,
                new Translation("de", "Mehl Type 405", "Weizenmehl für Kuchen und Gebäck"),
                new Translation("fr", "Farine type 405", "Farine de blé pour gâteaux et pâtisseries"),
                new Translation("en", "Flour Type 405", "Wheat flour for cakes and pastries")
        );
        createProduct("🌾", "from-amber-600 to-brown-700", 500, 10, bakingCat,
                new Translation("de", "Vollkornmehl", "Dunkles Weizenvollkornmehl"),
                new Translation("fr", "Farine complète", "Farine de blé complète foncée"),
                new Translation("en", "Wholemeal Flour", "Dark whole wheat flour")
        );
        createProduct("🌲", "from-amber-300 to-amber-500", 50, 1, bakingCat,
                new Translation("de", "Pinienkerne", "Naturbelassene Pinienkerne"),
                new Translation("fr", "Pignons de pin", "Pignons de pin naturels"),
                new Translation("en", "Pine Nuts", "Natural pine nuts")
        );

        // Süssungsmittel
        createProduct("🧂", "from-slate-100 to-slate-300", 500, 10, sweetCat,
                new Translation("de", "Zucker Weiss", "Kristallzucker raffiniert"),
                new Translation("fr", "Sucre blanc", "Sucre cristallisé raffiné"),
                new Translation("en", "White Sugar", "Refined crystal sugar")
        );
        createProduct("🧂", "from-amber-600 to-brown-700", 500, 10, sweetCat,
                new Translation("de", "Rohrzucker", "Brauner unraffinierter Rohrzucker"),
                new Translation("fr", "Sucre de canne", "Sucre de canne brun non raffiné"),
                new Translation("en", "Cane Sugar", "Unrefined brown cane sugar")
        );

        // Gewürze
        createProduct("🧂", "from-blue-200 to-blue-400", 500, 10, spiceCat,
                new Translation("de", "Salz Meersalz", "Natürliches Meersalz grob"),
                new Translation("fr", "Sel de mer", "Gros sel de mer naturel"),
                new Translation("en", "Sea Salt", "Natural coarse sea salt")
        );
        createProduct("🧂", "from-slate-400 to-slate-600", 100, 3, spiceCat,
                new Translation("de", "Pfeffer Schwarz", "Ganze schwarze Pfefferkörner"),
                new Translation("fr", "Poivre noir", "Poivre noir entier en grains"),
                new Translation("en", "Black Pepper", "Whole black peppercorns")
        );
        createProduct("🌶️", "from-red-400 to-orange-600", 100, 3, spiceCat,
                new Translation("de", "Paprikapulver", "Edelsüsses Paprikapulver"),
                new Translation("fr", "Paprika", "Poudre de paprika doux"),
                new Translation("en", "Paprika Powder", "Sweet paprika powder")
        );
        createProduct("🪵", "from-amber-600 to-brown-700", 50, 2, spiceCat,
                new Translation("de", "Zimt", "Gemahlener Ceylon Zimt"),
                new Translation("fr", "Cannelle", "Cannelle de Ceylan moulue"),
                new Translation("en", "Cinnamon", "Ground Ceylon cinnamon")
        );
        createProduct("🟡", "from-yellow-500 to-amber-600", 75, 2, spiceCat,
                new Translation("de", "Currypulver", "Milde Currymischung"),
                new Translation("fr", "Poudre de curry", "Mélange de curry doux"),
                new Translation("en", "Curry Powder", "Mild curry blend")
        );
        createProduct("🌿", "from-green-600 to-emerald-700", 30, 1, spiceCat,
                new Translation("de", "Oregano", "Getrockneter Oregano gerebelt"),
                new Translation("fr", "Origan", "Origan séché émondé"),
                new Translation("en", "Oregano", "Dried rubbed oregano")
        );

        // Snacks
        createProduct("🥜", "from-amber-600 to-brown-700", 200, 5, snacksCat,
                new Translation("de", "Nüsse Mix", "Cashew, Mandeln, Walnüsse"),
                new Translation("fr", "Mélange de noix", "Noix de cajou, amandes, noix"),
                new Translation("en", "Nut Mix", "Cashews, almonds, walnuts")
        );
        createProduct("🥜", "from-amber-600 to-brown-700", 150, 4, snacksCat,
                new Translation("de", "Studentenfutter", "Klassische Nuss-Frucht-Mischung"),
                new Translation("fr", "Mélange étudiant", "Mélange classique de noix et fruits secs"),
                new Translation("en", "Trail Mix", "Classic nut and dried fruit mix")
        );
        createProduct("🌰", "from-green-600 to-emerald-700", 100, 3, snacksCat,
                new Translation("de", "Pistazien", "Geröstete Pistazien mit Salz"),
                new Translation("fr", "Pistaches", "Pistaches grillées et salées"),
                new Translation("en", "Pistachios", "Roasted and salted pistachios")
        );
        createProduct("🎃", "from-green-600 to-emerald-700", 80, 2, snacksCat,
                new Translation("de", "Kürbiskerne", "Getrocknete Kürbiskerne"),
                new Translation("fr", "Graines de courge", "Graines de courge séchées"),
                new Translation("en", "Pumpkin Seeds", "Dried pumpkin seeds")
        );
        createProduct("🍿", "from-yellow-400 to-orange-500", 90, 3, snacksCat,
                new Translation("de", "Popcorn", "Gesalzenes Popcorn"),
                new Translation("fr", "Pop-corn", "Pop-corn salé"),
                new Translation("en", "Popcorn", "Salted popcorn")
        );
        createProduct("🥔", "from-yellow-500 to-amber-600", 150, 5, snacksCat,
                new Translation("de", "Kartoffelchips", "Knusprige Kartoffelchips"),
                new Translation("fr", "Chips de pommes de terre", "Chips de pommes de terre croustillantes"),
                new Translation("en", "Potato Chips", "Crunchy potato chips")
        );
        createProduct("🥨", "from-amber-500 to-orange-600", 120, 4, snacksCat,
                new Translation("de", "Salzbrezeln", "Kleine salzige Brezeln"),
                new Translation("fr", "Bretzels salés", "Petits bretzels salés"),
                new Translation("en", "Pretzels", "Small salty pretzels")
        );
        createProduct("🌮", "from-yellow-500 to-amber-600", 200, 5, snacksCat,
                new Translation("de", "Tortilla Chips", "Maischips mit Käsegeschmack"),
                new Translation("fr", "Chips tortilla", "Chips de maïs goût fromage"),
                new Translation("en", "Tortilla Chips", "Cheese flavored corn chips")
        );
        createProduct("🥜", "from-amber-500 to-orange-600", 150, 4, snacksCat,
                new Translation("de", "Erdnussflips", "Gebackene Erdnussflips"),
                new Translation("fr", "Flips aux cacahuètes", "Snacks soufflés à la cacahuète"),
                new Translation("en", "Peanut Flips", "Baked peanut puffs")
        );

        // Süssigkeiten
        createProduct("🍬", "from-purple-500 to-pink-600", 150, 5, sweetsCat,
                new Translation("de", "Jelly Beans", "Bunte Geleebohnen"),
                new Translation("fr", "Jelly Beans", "Bonbons gélifiés colorés"),
                new Translation("en", "Jelly Beans", "Colorful jelly beans")
        );
        createProduct("🧸", "from-red-400 to-orange-600", 200, 5, sweetsCat,
                new Translation("de", "Gummibärchen", "Fruchtige Gummibärchen"),
                new Translation("fr", "Oursons en gomme", "Bonbons gélifiés aux fruits"),
                new Translation("en", "Gummy Bears", "Fruity gummy bears")
        );
        createProduct("🍫", "from-indigo-500 to-blue-600", 125, 3, sweetsCat,
                new Translation("de", "Schokolinsen", "Bunte Schokolinsen"),
                new Translation("fr", "Dragées chocolatées", "Billes de chocolat colorées"),
                new Translation("en", "Chocolate Lentils", "Colorful candy-coated chocolate drops")
        );
        createProduct("🖤", "from-slate-400 to-slate-600", 175, 4, sweetsCat,
                new Translation("de", "Lakritz", "Süsse Lakritzschnecken"),
                new Translation("fr", "Réglisse", "Rouleaux de réglisse sucrés"),
                new Translation("en", "Licorice", "Sweet licorice wheels")
        );
        createProduct("☁️", "from-slate-100 to-slate-300", 100, 3, sweetsCat,
                new Translation("de", "Marshmallows", "Weiche Schaumzuckerware"),
                new Translation("fr", "Guimauves", "Guimauves moelleuses"),
                new Translation("en", "Marshmallows", "Soft marshmallow treats")
        );
        createProduct("🍏", "from-green-600 to-emerald-700", 150, 4, sweetsCat,
                new Translation("de", "Fruchtgummi", "Saure Apfelringe"),
                new Translation("fr", "Bonbons fruités", "Anneaux de pomme acides gélifiés"),
                new Translation("en", "Fruit Gummy", "Sour apple rings")
        );
        createProduct("🍫", "from-amber-600 to-brown-700", 100, 3, sweetsCat,
                new Translation("de", "Schokoladentafel", "Vollmilchschokolade"),
                new Translation("fr", "Tablette de chocolat", "Chocolat au lait classique"),
                new Translation("en", "Chocolate Bar", "Milk chocolate bar")
        );
        createProduct("🍬", "from-amber-500 to-orange-600", 150, 4, sweetsCat,
                new Translation("de", "Karamellbonbons", "Weiche Sahnekaramellbonbons"),
                new Translation("fr", "Bonbons au caramel", "Caramels mous à la crème"),
                new Translation("en", "Caramel Candies", "Soft cream caramel candies")
        );
        createProduct("🫧", "from-blue-200 to-blue-400", 50, 1, sweetsCat,
                new Translation("de", "Kaugummi", "Pfefferminz Kaugummidragees"),
                new Translation("fr", "Chewing-gum", "Dragées de chewing-gum à la menthe poivrée"),
                new Translation("en", "Chewing Gum", "Peppermint chewing gum pellets")
        );

        // Tee
        createProduct("🍵", "from-green-600 to-emerald-700", 100, 3, teaCat,
                new Translation("de", "Tee Earl Grey", "Schwarztee mit Bergamotte"),
                new Translation("fr", "Thé Earl Grey", "Thé noir aromatisé à la bergamote"),
                new Translation("en", "Earl Grey Tea", "Black tea infused with bergamot")
        );

        // Hülsenfrüchte
        createProduct("🫘", "from-red-400 to-orange-600", 500, 10, legumeCat,
                new Translation("de", "Linsen Rot", "Rote Linsen geschält"),
                new Translation("fr", "Lentilles rouges", "Lentilles rouges décortiquées"),
                new Translation("en", "Red Lentils", "Peeled red lentils")
        );

        System.out.println("Default products with translations created.");
    }

    // Angepasste Helper-Methode zur sauberen Verarbeitung der Übersetzungen
    private void createProduct(String icon, String color, int targetWeight, int tolerance,
                               Category category, Translation... translations) {

        ProductConfiguration product = new ProductConfiguration();
        product.setDefaultTargetWeight(targetWeight);
        product.setDefaultTolerance(tolerance);
        product.setIcon(icon);
        product.setColor(color);
        product.setActive(true);
        product.setCategory(category);

        // Zuerst die Hauptkonfiguration speichern, um die ID zu generieren
        ProductConfiguration savedProduct = productConfigurationRepository.save(product);

        // Übersetzungen zuordnen und in die Datenbank schreiben
        for (Translation t : translations) {
            ProductConfigurationTranslation translationEntity = new ProductConfigurationTranslation();
            translationEntity.setProductConfiguration(savedProduct);
            translationEntity.setLanguageCode(t.lang());
            translationEntity.setName(t.name());
            translationEntity.setDescription(t.desc());
            translationRepository.save(translationEntity);
        }
    }

    private Category createCategoryWithTranslations(String nameDe, String nameFr, String nameEn) {
        Category category = new Category();
        category.setTranslations(new ArrayList<>());

        // Zuerst die Hauptkategorie speichern, um die ID zu generieren
        Category savedCategory = categoryRepository.save(category);

        // Übersetzungen hinzufügen
        saveCategoryTranslation(savedCategory, "de", nameDe);
        saveCategoryTranslation(savedCategory, "fr", nameFr);
        saveCategoryTranslation(savedCategory, "en", nameEn);

        return savedCategory;
    }


    private void saveCategoryTranslation(Category category, String lang, String name) {
        CategoryTranslation translation = new CategoryTranslation();
        translation.setCategoryTranslation(category);
        translation.setLanguageCode(lang.toLowerCase());
        translation.setCategoryName(name);
        categoryTranslationRepository.save(translation);
    }
}