# Eksamen PGR301

## Oppgave - Devops

Forklare litt rundt forbedringer. Her vil det være mye å forbedre.

Konfigurer github for bedre flyt i utviklingsprosessen ved hjelp av branch protection:
Naviger til “settings” fanen i ditt repository og velg “branches”. 

--BILDE--

Her kan man sette opp “rules” for branches. Dette kan f.eks være at det ikke kan merges til “main” med mindre alle tester kjører og koden kompilerer.

Velg “add rule” og fyll inn navnet på branchen man vil protecte.

For å kreve at action workflows kjører før en eventuell merge velger man “Require status checks to pass before merging” og velger hvilke workflows som må fullføres. For å kreve “approval” av andre velger man “Require a pull request before merging” og “Require approvals”. Her kan man velge hvor mange som må godkjenne koden før en eventuell merge kan skje.

Bilde av teksten beskrevet over:

--BILDE--

For å få en effektiv flyt i utviklingen bør utviklere opprette en ny branch for hver oppgave de skal jobbe på, for å så lage en pull request til main igjen. Dersom branch protection er satt opp bra vil dette fungere bra. Branch navn kan f.eks være “feature/<navnpåoppgave>”.

Jeg har github pro (man får det gratis når man er student), så branch protection fungerte også i private repo for meg. 

## Oppgave - Pipeline

Ettersom applikasjonen ofte gir feil vil det være vanskelig å gjøre en ordentlig enhetstest mot servicen. I dette tilfellet vil det være gunstig om testen kjører hver gang for å demonstrerer at pipeline fungerer som det skal, derfor er ikke enhetstesten en “virkelig” service test.
Jeg har likevel lagt ved en test som kunne vært en ordentlig enhetstest, men kommentert ut.

## Oppgave - Feedback

Ettersom vi vet at *ReallyShakyBankingCoreSystemService* kommuniserer med kjernesystemet kan vi ved telemetri i APIet med fakta si i hvilket av systemene problemet oppstår. Dersom servicen kaster en BackendException har noe gått galt i kommunikasjonen til kjernesystemet. Derfor gjør jeg try/catch og catcher eventuelle exceptions som oppstår i servicen, da kan man ved bruk av en counter levere til influx når det oppstår en slik exception. 

Ved bruk av en timer kan jeg måle tiden kommunikasjonen til kjernesystemet bruker. Her ser man at det er utrolig stor forskjell fra request til request og derfor kan man si at kjernesystemet forårsaker de trege responstidene.

#### Spørringer mot Influx for å se telemetri

For å se tiden kjernesystemet har brukt på en request kan man skrive: 

    select * from responseTime where count > 0

--BILDE--

Her kan man se hvor lang tid kjernesystemet bruker og hvilken metode det gjelder. Her kan man også se at det ikke er APIet som skaper de trege responstidene.

For å se backend exceptions:

    select * from backendException where value > 0

For å se hvor mange ganger backend exception har skjedd

    select sum(*) from backendException where value > 0

Note til influx: Forklare det med 1s og count 0

#### Grafana dashboard

Jeg har laget et dashboard hvor man kan se metrics fra de forskjellig endpointene alene eller alle på likt. Det er også en for backend exceptions.

--BILDE--

#### Litt om logging

I Logback.xml var det konfigurert at det bare skulle logges til console dersom det var en “error”. Jeg endret dette til å være “info” slik at applikasjonen gir fra seg mer logging. Ved bruk av “Logger” klassen kan jeg enkelt logge til console i fra de forskjellige API endpointene.

Man kunne også f.eks konfigurert logging til å være “debug”, men dette ville gitt utrolig mange logger grunnet at applikasjonen hver sekund leverer data til influxdb.

## Oppgave - Terraform

Første gang man kjører terraform kode (uten backend) vil det opprettes en lokal state fil. Dette kan skape problemer når man kjører koden på en annen maskin. Da vil det opprettes en ny state fil og når det skal opprettes som i Skalbank sitt tilfelle en s3 bucket, vil den prøve å opprette en ny fordi den finnes ikke i state filen. Slik får man feilmeldingen med at s3 bucket finnes fra før fordi den prøver å opprette den på nytt. Hadde man hatt en state fil lik for alle (f.eks å ha backend) så ville ikke koden prøve å opprette en ny bucket dersom den lå i state filen. 

Derfor vil dette funke første gang Jens kjører det lokalt hos seg, men ikke hos andre etter han allerede har opprettet denne bucketen. Når han sletter state filen vil ha støte på samme problem som alle andre, forklart over.

#### Opprette en s3 bucket i AWS

For å lage s3 bucket fra CLI, må man først hente nøkler/credentials i AWS. For å gjøre dette i AWS Console må man først gå til servicen “IAM”, velge brukeren man vil hente/lage nøkler for, velge “Security credentials” panelet og trykke på “Create access key”. Her får man bare se sin secret én gang. Derfor er det sterkt anbefalt å lagre denne secret nøkkelen på et sikkert sted.

Neste steg vil være å identifisere seg mot AWS på sin kommandolinje. Dette kan gjøres ved å skrive “aws configure” og paste inn nøklene.

Etter suksessfull identifisering kan man gjøre denne kommandoen for å opprette en s3 bucket på regionen eu-west-1 med navn pgr301-maen030-terraform:

    aws s3api create-bucket --bucket pgr301-maen030-terraform --region eu-west-1 --create-bucket-configuration LocationConstraint=eu-west-1

Terraform kode og pipeline er opprettet og fungerer som det skal. Jeg valgte å lage en branch i mitt eget repo for å teste at terraform kode ikke blir appliet med mindre det merges til main, men det sjekkes at det er gyldig kode.

#### Terraform i pipeline

Når sensor forker dette repo er det viktig at han/hun har nøkler i AWS slik at han/hun kan identifisere seg. I repo må disse nøklene legges inn under “Secrets” hvor access key har navnet “AWS_ACCESS_KEY_ID” og secret har navnet “AWS_SECRET_ACCESS_KEY”.

Slik som på dette bildet:

--BILDE--

Dersom sensor skal opprette ny backend for statefil i s3, må sensor først opprette en bucket for dette (f.eks i CLI) og så endre navnet på bucket i backend i provider.tf.

Det samme gjelder for ECR resourcen, dersom sensor skal opprette et nytt ECR repo må det endres til et nytt navn.

Det må også endres i workflowen “ecr.yml” til navnet på ECR repo som sensor har valgt.

## Oppgave - Docker

Vi får oppgitt i oppgaven at applikasjonen, i dockerfilen, skal bygges på jdk-18. Jeg har testet og dette fungerer, men jeg funderer på om dette er lurt ettersom kompileringen gjøres med “maven:3.6-jdk-11”.

*Hva vil kommandolinje for å bygge et container image være?*

    docker build -t maen030/skallbankapi .

*Hva vil kommandolinje for å starte en container være?*

Jeg tar utgangspunkt i at applikasjonen kjører på port 8080.

Starte på port 7777:

    docker run -d -p 7777:8080 --name skallbankapi maen030/skallbankapi

På både port 7777 og 8888:

    docker run -d -p 7777:8080 --name skallbankapi maen030/skallbankapi
    docker run -d -p 8888:8080 --name skallbankapi maen030/skallbankapi

Etter man har laget og startet en container fra et image, kan man stoppe og starte den samme containeren med docker stop/start <navn/id>

## Tilleggsinformasjon

Jeg har valgt å ha workflowene delt opp i egne filer for bedre oversikt under “Actions” fanen i repo.

Jeg har lagt til influxdb.conf i repo ettersom denne må være med for å starte influx.

Kandidatnr:

    2014
