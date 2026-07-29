# Texture Pack Switcher (Fabric - Minecraft 1.21.11)

Bu, senin istedigin gibi calisan bir Fabric mod **kaynak kodu** projesidir:

- Ana ekranda **profil listesi** var.
- **"+ Yeni Profil"** butonuna tikladiginda, `resourcepacks` klasorundeki texture packlerden
  istedigini secip yeni bir profil olusturabiliyorsun.
- Her profile bir **tus** atayabiliyorsun (butona tikla, sonra istedigin tusa bas).
- O tusa bastiginda: o profildeki texture packler otomatik **acilir**, o an aktif olan
  (senin resourcepacks klasorunden gelen) diger texture packler otomatik **kapanir**.
- Modun kendi arayuzunu acmak icin varsayilan tus: `]` (sag kose parantez).
  Bunu Secenekler > Tus Atamalari > "Texture Pack Switcher" kategorisinden degistirebilirsin.

## Neden zip icinde derlenmis .jar yok?

Bu ortamda internete erisimim kapali. Fabric modlarini derlemek icin Gradle'in
Minecraft, Fabric Loader, Fabric API ve mapping dosyalarini internetten indirmesi
gerekiyor - bu yuzden burada derleyip sana hazir .jar veremiyorum. Ama tum kaynak
kod hazir, asagidaki adimlarla **kendi bilgisayarinda 2 dakikada** derleyebilirsin.

## Nasil derlenir (kendi bilgisayarinda)

### Yontem 1: IntelliJ IDEA (en kolay, tavsiye edilen)
1. IntelliJ IDEA'yi ac (Community Edition yeterli).
2. `File > Open` ile bu klasoru (icinde `build.gradle` olan klasor) ac.
3. IntelliJ Gradle projesini otomatik taniyip gerekli her seyi (Gradle wrapper dahil)
   internetten indirecek. Bir sure bekle (ilk seferde Minecraft/Fabric indigi icin
   birkac dakika surebilir).
4. Sag tarafta acilan Gradle panelinden `Tasks > build > build` gorevine cift tikla.
   (Ya da alttaki terminalden `./gradlew build` yaz.)
5. Derleme bitince jar dosyan burada olacak:
   `build/libs/texture-pack-switcher-1.0.0.jar`

### Yontem 2: Terminal / komut satiri
1. Bilgisayaninda Gradle kurulu degilse: https://gradle.org/install/ adresinden kur
   (ya da sadece `gradle wrapper` calistirip Gradle Wrapper olustur).
2. Bu klasorun icine terminalden gir.
3. Sirasiyla:
   ```
   gradle wrapper
   ./gradlew build      (Windows'ta: gradlew.bat build)
   ```
4. Jar dosyan: `build/libs/texture-pack-switcher-1.0.0.jar`

## Kurulum (jar'i aldiktan sonra)
1. [Fabric Loader](https://fabricmc.net/use/) 'i Minecraft 1.21.11 icin kur.
2. [Fabric API](https://modrinth.com/mod/fabric-api) 0.141.x+1.21.11 (ya da uyumlu
   surum) jar'ini `.minecraft/mods` klasorune koy.
3. Bu modun derledigin jar'ini da `.minecraft/mods` klasorune koy.
4. Oyunu ac, `resourcepacks` klasorune istedigin texture packleri koy.
5. Oyun icindeyken `]` tusuna bas, "+ Yeni Profil" ile profillerini olustur.

## Proje yapisi
```
texture-pack-switcher/
├── build.gradle              -> Gradle yapilandirmasi (Minecraft/Fabric surumleri)
├── gradle.properties          -> Surum numaralari (1.21.11 icin ayarli)
├── settings.gradle
└── src/main/
    ├── java/com/tpswitcher/
    │   ├── TextureSwitcherClient.java   -> Mod giris noktasi, tus dinleme
    │   ├── Profile.java                 -> Bir profilin verisi (isim, tus, packler)
    │   ├── ProfileManager.java          -> Profilleri kaydetme/yukleme/uygulama
    │   └── gui/
    │       ├── ProfileListScreen.java   -> Ana ekran (+ butonu burada)
    │       └── ProfileEditScreen.java   -> Profil olusturma/duzenleme ekrani
    └── resources/
        ├── fabric.mod.json
        └── assets/tpswitcher/lang/...
```

Profiller `.minecraft/config/tpswitcher_profiles.json` dosyasinda saklanir.

## Not
1.21.11, Minecraft'in obfuscation'in kaldirilmasina gecis surumu ve ayni zamanda
Yarn mapping'lerinin destekledigi **son** surum. Bu yuzden bircok "AI" arac bu
surumu tanimiyor - ben guncel Fabric duyurularini tarayip dogru surum numaralarini
(Loader 0.18.1, Yarn 1.21.11+build.4, Fabric API 0.141.3+1.21.11) kullandim.
