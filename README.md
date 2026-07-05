# proiectAWBD
Sistem de Programari Medicale

**Cerințe funcționale**
1. Sistemul trebuie să permită înregistrarea și gestionarea profilurilor de pacient și de medic.
2. Sistemul trebuie să permită autentificarea utilizatorilor pe baza unui cont și a unui rol (pacient, medic sau administrator).
3. Sistemul trebuie să permită pacientului vizualizarea listei serviciilor medicale disponibile.
4. Sistemul trebuie să permită afișarea intervalelor orare disponibile pentru programarea unui serviciu medical.
5. Sistemul trebuie să permită crearea unei programări doar dacă intervalul selectat este disponibil.
6. Sistemul trebuie să împiedice efectuarea unei programări într-un interval ocupat, în afara programului medicului sau într-o perioadă de concediu.
7. Sistemul trebuie să permită pacientului vizualizarea tuturor programărilor sale.
8. Sistemul trebuie să permită medicului înregistrarea zilelor de concediu (PTO).
9. Sistemul trebuie să verifice existența programărilor înainte de aprobarea unei cereri de concediu și să respingă solicitarea dacă există consultații programate.
10. Sistemul trebuie să permită actualizarea statusului unei programări (de exemplu: Appointed, Completed, Cancelled).
11. Sistemul trebuie să genereze automat informațiile privind plata unei programări și să stabilească dacă aceasta este acoperită de asigurare sau abonament.
12. Sistemul trebuie să permită pacientului acordarea unui rating pentru serviciul medical doar după finalizarea consultației.
13. Sistemul trebuie să recalculeze automat ratingul mediu al unui serviciu medical după înregistrarea unui nou feedback.
14. Sistemul trebuie să returneze mesaje de eroare corespunzătoare atunci când o operație nu poate fi efectuată (de exemplu, programare invalidă, medic inexistent, pacient inexistent).
15. Sistemul trebuie să asigure accesul la funcționalități în funcție de rolul utilizatorului (pacient, medic, administrator).

Entități

Sistemul de management medical utilizează un model relațional complex format din 9 entități interconectate. Arhitectura bazei de date a fost concepută pentru a gestiona fluxul complet al unei clinici: de la autentificare și profiluri de utilizatori (Doctori/Pacienți), până la logica de programări, plăți și scheme de asigurare/abonament.

Relații @OneToOne
User => Patient / Doctor: Fiecare User este legat de un singur profil clinic. Acest lucru permite separarea datelor de autentificare de datele medicale.
Appointment => Payment: O programare are asociată o singură tranzacție financiară, asigurând trasabilitatea plăților.

Relații @ManyToOne/@OneToMany 
Doctor => MedicalService: Mai mulți doctori pot fi specializați pe același serviciu medical.
Patient => Appointment: Un pacient poate avea mai multe programări de-a lungul timpului.
Doctor => PaidTimeOff: Un doctor poate solicita mai multe perioade de concediu.
InsuranceProvider => Patient: Mai mulți pacienți pot fi arondați aceluiași furnizor de asigurări medicale.

Relație @ManyToMany
MedicalService => InsuranceProvider: Implementată prin tabelul de joncțiune SERVICE_INSURANCE_COVERAGE. Un serviciu medical poate fi acoperit de mai mulți asiguratori, iar un asigurator poate acoperi o gamă largă de servicii.

Diagrama ER
<img width="967" height="653" alt="image" src="https://github.com/user-attachments/assets/6bfe2178-60d7-4190-9c50-237f974c9171" />

Diagrama conceptuala 
<img width="975" height="970" alt="image" src="https://github.com/user-attachments/assets/3e452899-61fa-4d6d-b37c-047688b51320" />


