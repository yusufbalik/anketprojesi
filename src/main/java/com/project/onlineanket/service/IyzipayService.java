package com.project.onlineanket.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.project.onlineanket.entity.Kullanici;

@Service
public class IyzipayService {

    // --- IYZICO SANDBOX AYARLARI ---
    private String baseUrl = "https://sandbox-api.iyzipay.com";
    private String apiKey = "sandbox-GumT5dWZcKOyi07Izd7n99qThRhMEevc"; 
    private String secretKey = "sandbox-BT1qYSsSYG6GS22iRJ9XEYkUbUHlMI2u";

    // --- 1. METOD: WEB İÇİN (Varsayılan localhost) ---
    // Mevcut web kodlarını bozmamak için bu metod localhost kullanır.
    public CheckoutFormInitialize odemeFormuOlustur(Kullanici kullanici, String paketTipi, String fiyatStr) {
        return odemeFormuOlustur(kullanici, paketTipi, fiyatStr, "http://localhost:8080/odeme-sonuc");
    }

    // --- 2. METOD: MOBİL İÇİN (Dinamik Callback URL) ---
    // Mobil controller buradan Ngrok URL'ini gönderecek.
    public CheckoutFormInitialize odemeFormuOlustur(Kullanici kullanici, String paketTipi, String fiyatStr, String callbackUrl) {
        
        // 1. AYARLAR
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);

        // 2. İSTEK
        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId("123456789");
        
        BigDecimal fiyat = new BigDecimal(fiyatStr);
        request.setPrice(fiyat);
        request.setPaidPrice(fiyat);
        request.setCurrency(Currency.TRY.name());
        request.setBasketId("B" + System.currentTimeMillis());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        
        // DİKKAT: Burası artık parametreden gelen URL'i kullanıyor
        request.setCallbackUrl(callbackUrl);
        
        request.setEnabledInstallments(new ArrayList<Integer>());

        // 3. ALICI (BUYER)
        Buyer buyer = new Buyer();
        buyer.setId(kullanici.getId().toString());
        buyer.setName(kullanici.getKullaniciAdi());
        buyer.setSurname("Uye");
        buyer.setGsmNumber("+905350000000");
        buyer.setEmail(kullanici.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setLastLoginDate("2023-01-01 11:11:11");
        buyer.setRegistrationDate("2023-01-01 11:11:11");
        buyer.setRegistrationAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        buyer.setIp("85.34.78.112");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34732");
        request.setBuyer(buyer);

        // 4. ADRES
        Address billingAddress = new Address();
        billingAddress.setContactName(kullanici.getKullaniciAdi());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        billingAddress.setZipCode("34732");
        request.setBillingAddress(billingAddress);
        request.setShippingAddress(billingAddress);

        // 5. SEPET
        List<BasketItem> basketItems = new ArrayList<>();
        BasketItem item = new BasketItem();
        item.setId("BI101");
        item.setName(paketTipi + " Premium Üyelik");
        item.setCategory1("Yazılım");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(fiyat);
        basketItems.add(item);
        request.setBasketItems(basketItems);

        // 6. OLUŞTUR
        return CheckoutFormInitialize.create(request, options);
    }
}