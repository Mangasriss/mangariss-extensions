import json
import os

# CONFIGURATION
# C'est ce qui définit ton lien final
REPO_BASE_URL = "https://raw.githubusercontent.com/Mangasriss/mangariss-extensions/repo"
APK_NAME = "mangariss.apk"
PKG_NAME = "eu.kanade.tachiyomi.extension.fr.mangariss"

def main():
    # 1. Récupération de la version depuis l'environnement (passé par GitHub Actions)
    version_code = int(os.environ.get("VERSION_CODE", 1))
    
    # Le common.gradle définit le format "1.4.x" pour le versionName
    version_name = f"1.4.{version_code}" 

    print(f"Génération index.json pour Mangariss v{version_name} ({version_code})")

    # 2. Création de l'objet JSON pour Mihon
    extension_data = {
        "name": "Mangariss",
        "pkg": PKG_NAME,
        "apk": APK_NAME,
        "lang": "fr",
        "code": version_code,
        "version": version_name,
        "nsfw": 0,
        "icon": "icon.png" # L'icône sera copiée par le workflow
    }

    # 3. Écriture du fichier index.min.json
    output = [extension_data]
    
    with open("index.min.json", "w") as f:
        json.dump(output, f, separators=(',', ':'))

if __name__ == "__main__":
    main()