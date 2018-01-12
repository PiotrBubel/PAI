# Renting Rooms
### Wymagania
Aplikacja Renting Rooms została przystosowana do uruchamiania w kontenerach programu Docker, na 
komputerach z systemem operacyjnym Linux.
Aplikacja wymaga zainstalowanego programu Docker i Docker-compose.

Instrukcja instalacji programu Docker dostępna jest na stronie: 
https://docs.docker.com/engine/installation/
 
Instrukcja instalacji programu Docker-compose dostępna jest na stronie: 
https://docs.docker.com/compose/install/

### Instrukcje zbudowania i uruchomienia aplikacji Renting Rooms:
Po zainstalowaniu potrzebnych programów aplikację można uruchomić wpisując w konsoli:

> mvn clean package

Polecenie to zbuduje aplikację i stworzy obrazy programu Docker

> docker-compose up

Polecenie to uruchomi i odpowiednio połączy ze sobą kontenery programu Docker

Po wykonaniu powyższych poleceń, aplikacja Renting Rooms będzie uruchomiona, w konsoli będą 
wypisywane logi, a dostęp do aplikacji będzie możliwy poprzez otworzenie w przeglądarce internetowej 
adresu: 
localhost:8097

Istnieje także możliwość uruchomienia aplikacji za pomocą stworzonych do tego celu skryptów: 

> build-and-run.sh

Który buduje aplikacje, tworzy obrazy i kontenery Docker'a i uruchamia program z podglądem logów w konsoli.

> logs-build-and-run.sh

Który buduje aplikacje, tworzy obrazy i kontenery Docker'a i uruchamia program z zapisem logów do plików,
logi budowania aplikacji zapisane zostaną do pliku build-logs-2.txt, a logi działania aplikacji do pliku:
run-logs-2.txt

### Instrukcje zbudowania pojedynczych mikroserwisów:
Aplikacja składa się z mikroserwisów i mogą one być budowane i uruchamiane niezależnie. Aby zbudować i 
stworzyć obraz Docker'a dla każdego z mikroserwisów, należy przejść do katalogu zawierającego wybrany 
mikroserwis, a następnie wpisać w konsoli polecenie:

> mvn clean package

Dzięki temu uzyskamy zbudowany program wraz z odpowiednim obrazem Docker'a gotowym do uruchomienia.

