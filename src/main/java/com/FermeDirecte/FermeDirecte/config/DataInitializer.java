package com.FermeDirecte.FermeDirecte.config;

import com.FermeDirecte.FermeDirecte.entity.*;
import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import com.FermeDirecte.FermeDirecte.enums.OrderStatus;
import com.FermeDirecte.FermeDirecte.enums.PaymentStatus;
import com.FermeDirecte.FermeDirecte.enums.Role;
import com.FermeDirecte.FermeDirecte.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Ne rien faire si les données existent déjà
        if (userRepository.count() > 0) {
            log.info("Données déjà présentes, initialisation ignorée.");
            return;
        }

        log.info("Initialisation des données de test...");

        String pwd = passwordEncoder.encode("password123");

        // ── 1. USERS ──────────────────────────────────────────────
        User admin   = save(user("admin@fermedirecte.tn",    pwd, "Mohamed", "Ben Ali",  "+216 20 123 456", Role.ADMIN));
        User seller1 = save(user("ferme.oliviers@gmail.com", pwd, "Ahmed",   "Trabelsi", "+216 22 345 678", Role.SELLER));
        User seller2 = save(user("jardin.bio@gmail.com",     pwd, "Fatma",   "Khelifi",  "+216 23 456 789", Role.SELLER));
        User seller3 = save(user("elevage.sahel@gmail.com",  pwd, "Karim",   "Mansour",  "+216 24 567 890", Role.SELLER));
        User seller4 = save(user("fruits.capbon@gmail.com",  pwd, "Leila",   "Hamdi",    "+216 25 678 901", Role.SELLER));
        User client1 = save(user("client1@gmail.com",        pwd, "Sami",    "Bouazizi", "+216 26 789 012", Role.CUSTOMER));
        User client2 = save(user("client2@gmail.com",        pwd, "Amira",   "Jebali",   "+216 27 890 123", Role.CUSTOMER));
        User client3 = save(user("client3@gmail.com",        pwd, "Youssef", "Gharbi",   "+216 28 901 234", Role.CUSTOMER));

        // ── 2. SELLER PROFILES ────────────────────────────────────
        SellerProfile sp1 = saveProfile(seller1, "Ferme des Oliviers",  "Producteur huile olive extra vierge bio depuis 1985. Sfax.", 4.8);
        SellerProfile sp2 = saveProfile(seller2, "Jardin Bio de Nabeul","Legumes et fruits biologiques sans pesticides.",             4.6);
        SellerProfile sp3 = saveProfile(seller3, "Elevage du Sahel",    "Viandes et produits laitiers fermiers. Plein air.",          4.9);
        SellerProfile sp4 = saveProfile(seller4, "Fruits du Cap Bon",   "Agrumes, fraises et fruits de saison.",                     4.7);

        // ── 3. ADDRESSES ──────────────────────────────────────────
        Address adr1 = saveAddress(client1, "Mohamed", "Ben Ali",  "42 Rue de la Republique", "Tunis",  "1001", true);
        Address adr2 = saveAddress(client1, "Mohamed", "Ben Ali",  "8 Avenue Mohamed V",      "Ariana", "2080", false);
        Address adr3 = saveAddress(client2, "Amira",   "Jebali",   "23 Rue Ibn Khaldoun",     "Sousse", "4000", true);
        Address adr4 = saveAddress(client3, "Youssef", "Gharbi",   "67 Avenue de la Liberte", "Sfax",   "3000", true);

        // ── 4. CATEGORIES ─────────────────────────────────────────
        Category cFruits   = saveCat("Fruits et Legumes", "Produits frais de saison",         null);
        Category cLaitiers = saveCat("Produits Laitiers", "Lait, fromages, yaourts fermiers", null);
        Category cViandes  = saveCat("Viandes",           "Viandes fraiches fermiers",        null);
        Category cEpicerie = saveCat("Epicerie",          "Huiles, miels, conserves",         null);
        Category cLegumes  = saveCat("Legumes",           "Legumes frais bio",                cFruits);
        Category cFruit    = saveCat("Fruits",            "Fruits de saison",                 cFruits);
        Category cAgrumes  = saveCat("Agrumes",           "Oranges, citrons, mandarines",     cFruit);
        Category cFromages = saveCat("Fromages",          "Fromages fermiers artisanaux",     cLaitiers);
        Category cYaourts  = saveCat("Yaourts",           "Yaourts nature et aromatises",     cLaitiers);
        Category cVRouge   = saveCat("Viande Rouge",      "Boeuf, agneau, mouton",            cViandes);
        Category cVolaille = saveCat("Volaille",          "Poulet, dinde fermiers",           cViandes);
        Category cHuiles   = saveCat("Huiles",            "Huiles olive et vegetales",        cEpicerie);

        // ── 5. PRODUCTS ───────────────────────────────────────────
        Product p1  = saveProd(sp1, "Huile Olive Extra Vierge Bio 1L",    "Premiere pression a froid. Acidite < 0.5%.",  "35.00", "32.00", 150, "litre",          Set.of(cEpicerie, cHuiles));
        Product p2  = saveProd(sp1, "Huile Olive Extra Vierge Bio 500ml", "Meme qualite en format economique.",          "18.00", null,    200, "bouteille",       Set.of(cEpicerie, cHuiles));
        Product p3  = saveProd(sp1, "Olives Noires Marinees 500g",        "Olives de table marinees aux herbes.",        "12.00", null,     80, "pot",             Set.of(cEpicerie));
        Product p4  = saveProd(sp2, "Tomates Bio",                        "Tomates coeur de boeuf sans pesticides.",    "4.50",  "3.90",  300, "kg",              Set.of(cFruits, cLegumes));
        Product p5  = saveProd(sp2, "Courgettes Bio",                     "Courgettes fraiches recoltees le matin.",    "3.20",  null,    250, "kg",              Set.of(cFruits, cLegumes));
        Product p6  = saveProd(sp2, "Salade Verte Bio",                   "Laitue croquante cultivee en plein champ.",  "2.50",  null,    180, "piece",           Set.of(cFruits, cLegumes));
        Product p7  = saveProd(sp2, "Carottes Bio",                       "Carottes sucrees et croquantes.",            "3.80",  null,    220, "kg",              Set.of(cFruits, cLegumes));
        Product p8  = saveProd(sp2, "Pommes de Terre Bio",                "Variete Spunta. Ideales pour frites.",       "2.80",  "2.50",  500, "kg",              Set.of(cFruits, cLegumes));
        Product p9  = saveProd(sp3, "Viande de Boeuf Entrecote",          "Viande maturee 21 jours. Plein air.",        "45.00", null,     50, "kg",              Set.of(cViandes, cVRouge));
        Product p10 = saveProd(sp3, "Poulet Fermier Entier",              "Poulet eleve en liberte. ~1.8kg.",           "22.00", "19.90",  80, "piece",           Set.of(cViandes, cVolaille));
        Product p11 = saveProd(sp3, "Fromage de Chevre Frais 250g",       "Fromage artisanal au lait cru.",             "8.50",  null,     60, "piece",           Set.of(cLaitiers, cFromages));
        Product p12 = saveProd(sp3, "Yaourt Nature Fermier 500g",         "Yaourt au lait entier sans additifs.",       "4.20",  null,    120, "pot",             Set.of(cLaitiers, cYaourts));
        Product p13 = saveProd(sp3, "Lait Frais Entier 1L",               "Lait cru pasteurise.",                       "3.50",  null,    100, "litre",           Set.of(cLaitiers));
        Product p14 = saveProd(sp4, "Oranges Maltaises",                  "Oranges juteuses et sucrees.",               "3.50",  "2.90",  400, "kg",              Set.of(cFruits, cFruit, cAgrumes));
        Product p15 = saveProd(sp4, "Citrons Bio",                        "Citrons non traites.",                       "4.00",  null,    200, "kg",              Set.of(cFruits, cFruit, cAgrumes));
        Product p16 = saveProd(sp4, "Fraises de Saison",                  "Fraises parfumees sous serre.",              "12.00", "10.50", 150, "barquette 500g",  Set.of(cFruits, cFruit));
        Product p17 = saveProd(sp4, "Mandarines",                         "Mandarines sucrees sans pepins.",            "3.80",  null,    300, "kg",              Set.of(cFruits, cFruit, cAgrumes));
        Product p18 = saveProd(sp4, "Pommes Golden",                      "Pommes croquantes et sucrees.",              "5.50",  null,    250, "kg",              Set.of(cFruits, cFruit));

        // ── 6. COUPONS ────────────────────────────────────────────
        saveCoupon("BIENVENUE20", "Bienvenue -20%",             new BigDecimal("20"), null,           false, "50.00",  null,    100);
        saveCoupon("PROMO15",     "Promotion -15%",             new BigDecimal("15"), null,           false, "30.00",  null,    200);
        saveCoupon("REDUC10",     "Reduction de 10 DT",         null,                 new BigDecimal("10"), false, "40.00", null, 150);
        saveCoupon("LIVRAISON",   "Livraison gratuite",         null,                 null,           true,  null,     null,    500);
        saveCoupon("MEGA50",      "Mega promo -50%",            new BigDecimal("50"), null,           false, "100.00", "30.00", 50);

        // ── 7. CARTS ──────────────────────────────────────────────
        Cart cart1 = cartRepository.save(Cart.builder().user(client1).build());
        Cart cart2 = cartRepository.save(Cart.builder().user(client2).build());
        Cart cart3 = cartRepository.save(Cart.builder().user(client3).build());

        // ── 8. ORDERS ─────────────────────────────────────────────
        Coupon coupon1 = couponRepository.findByCodeIgnoreCase("BIENVENUE20").orElse(null);
        Coupon coupon3 = couponRepository.findByCodeIgnoreCase("REDUC10").orElse(null);

        Order o1 = saveOrder(client1, adr1, coupon1, "CMD-2024-001", OrderStatus.DELIVERED,  PaymentStatus.PAID,    "85.50", "17.10", "7.00", "75.40");
        Order o2 = saveOrder(client2, adr3, null,    "CMD-2024-002", OrderStatus.SHIPPED,    PaymentStatus.PAID,    "52.30", "0.00",  "7.00", "59.30");
        Order o3 = saveOrder(client3, adr4, coupon3, "CMD-2024-003", OrderStatus.PROCESSING, PaymentStatus.PAID,    "95.00", "10.00", "7.00", "92.00");
        Order o4 = saveOrder(client1, adr1, null,    "CMD-2024-004", OrderStatus.PENDING,    PaymentStatus.PENDING, "45.00", "0.00",  "7.00", "52.00");

        // ── 9. REVIEWS ────────────────────────────────────────────
        saveReview(client1, p1,  5, "Excellente huile olive ! Gout fruite et authentique.");
        saveReview(client2, p10, 5, "Poulet fermier de qualite exceptionnelle. Chair tendre.");
        saveReview(client3, p14, 4, "Oranges bien juteuses. Bon rapport qualite/prix.");
        saveReview(client1, p4,  5, "Tomates bio delicieuses ! Vrai gout de tomate.");
        saveReview(client2, p8,  4, "Bonnes pommes de terre. Parfaites pour les frites.");
        saveReview(client3, p16, 5, "Fraises parfumees et sucrees. Mes enfants adorent !");

        log.info("✅ Données de test insérées avec succès !");
        log.info("   Admin    : admin@fermedirecte.tn / password123");
        log.info("   Vendeur  : ferme.oliviers@gmail.com / password123");
        log.info("   Client   : client1@gmail.com / password123");
    }

    // ── Helpers ───────────────────────────────────────────────────

    private User user(String email, String pwd, String prenom, String nom, String tel, Role role) {
        return User.builder().email(email).motDePasse(pwd).prenom(prenom).nom(nom)
                .telephone(tel).role(role).actif(true).build();
    }

    private User save(User u) { return userRepository.save(u); }

    private SellerProfile saveProfile(User user, String nom, String desc, double note) {
        return sellerProfileRepository.save(
                SellerProfile.builder().user(user).nomBoutique(nom).description(desc).note(note).build());
    }

    private Address saveAddress(User user, String prenom, String nom, String rue, String ville, String cp, boolean principal) {
        return addressRepository.save(
                Address.builder().user(user).prenom(prenom).nom(nom).rue(rue)
                        .ville(ville).codePostal(cp).pays("Tunisie").principal(principal).build());
    }

    private Category saveCat(String nom, String desc, Category parent) {
        return categoryRepository.save(Category.builder().nom(nom).description(desc).parent(parent).build());
    }

    private Product saveProd(SellerProfile sp, String nom, String desc, String prix, String prixPromo,
                              int stock, String unite, Set<Category> cats) {
        Product p = Product.builder()
                .sellerProfile(sp).nom(nom).description(desc)
                .prix(new BigDecimal(prix))
                .prixPromo(prixPromo != null ? new BigDecimal(prixPromo) : null)
                .stock(stock).actif(true).unite(unite)
                .imageUrl("https://via.placeholder.com/400x400?text=" + nom.replace(" ", "+"))
                .categories(cats)
                .build();
        return productRepository.save(p);
    }

    private void saveCoupon(String code, String desc, BigDecimal pct, BigDecimal fixe,
                             boolean livraison, String min, String max, int usagesMax) {
        Coupon c = Coupon.builder()
                .code(code).description(desc)
                .pourcentageReduction(pct)
                .montantFixeReduction(fixe)
                .livraisonGratuite(livraison)
                .montantMinimum(min != null ? new BigDecimal(min) : null)
                .montantMaximumReduction(max != null ? new BigDecimal(max) : null)
                .scope(CouponScope.GLOBAL)
                .usagesMaxGlobal(usagesMax).usagesActuels(0).usagesMaxParUtilisateur(1)
                .dateDebut(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(60))
                .actif(true).bloque(false)
                .build();
        couponRepository.save(c);
    }

    private Order saveOrder(User client, Address adr, Coupon coupon, String num,
                             OrderStatus statut, PaymentStatus paiement,
                             String sousTotal, String remise, String frais, String total) {
        return orderRepository.save(Order.builder()
                .client(client).adresseLivraison(adr).coupon(coupon)
                .numeroCommande(num).statut(statut).statutPaiement(paiement)
                .sousTotal(new BigDecimal(sousTotal))
                .remise(new BigDecimal(remise))
                .fraisLivraison(new BigDecimal(frais))
                .totalTTC(new BigDecimal(total))
                .build());
    }

    private void saveReview(User client, Product produit, int note, String commentaire) {
        reviewRepository.save(Review.builder()
                .client(client).produit(produit).note(note)
                .commentaire(commentaire).approuve(true).build());
    }
}
