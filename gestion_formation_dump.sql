--
-- PostgreSQL database dump
--

\restrict 1OFxcyqLw3zEv8MHLU3QEv810K3AWAPkxlDzE4kutwb6cjnQnkfuk30gFOdVA0u

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.cursus DROP CONSTRAINT IF EXISTS fktfpoco4anexe2mx4fycanxh9m;
ALTER TABLE IF EXISTS ONLY public.inscription_cours DROP CONSTRAINT IF EXISTS fkrgbjflyb7d08536o4iwwyo6hu;
ALTER TABLE IF EXISTS ONLY public.cours DROP CONSTRAINT IF EXISTS fkpron8fnvmqcw5nkrrn1576m3q;
ALTER TABLE IF EXISTS ONLY public.rythme DROP CONSTRAINT IF EXISTS fkplrj2pwsxg1miql9lg2kkokj7;
ALTER TABLE IF EXISTS ONLY public.promotion_cours DROP CONSTRAINT IF EXISTS fkof99okhawxx4r0kih8gevy7l4;
ALTER TABLE IF EXISTS ONLY public.cours_planifie DROP CONSTRAINT IF EXISTS fkn9m3a5ef4lq5j0ah4hf9npkm4;
ALTER TABLE IF EXISTS ONLY public.cursus_cours DROP CONSTRAINT IF EXISTS fklpj3xquqsobf77kcc6plfu0sh;
ALTER TABLE IF EXISTS ONLY public.cours_formateurs DROP CONSTRAINT IF EXISTS fkij72rjkjgaj5sovrxvv07aeka;
ALTER TABLE IF EXISTS ONLY public.promotion DROP CONSTRAINT IF EXISTS fkij3ht1gimy6e0o66m5bhtmg7t;
ALTER TABLE IF EXISTS ONLY public.cours_planifie DROP CONSTRAINT IF EXISTS fkih4sweo6debkljw4j6jmymxtu;
ALTER TABLE IF EXISTS ONLY public.cours_prerequis DROP CONSTRAINT IF EXISTS fkeoyeyjg4jqaloy9lm1jl969f6;
ALTER TABLE IF EXISTS ONLY public.promotion_cours DROP CONSTRAINT IF EXISTS fke4hvytgku0ixrlx66ynjclp65;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS fk9ximuq2tyddafxqlv006h4e29;
ALTER TABLE IF EXISTS ONLY public.cours_formateurs DROP CONSTRAINT IF EXISTS fk9eqp2hqblnovqmfbecuhvbkjm;
ALTER TABLE IF EXISTS ONLY public.cursus_cours DROP CONSTRAINT IF EXISTS fk6dlnqb0u7il9qxisik7vce4qj;
ALTER TABLE IF EXISTS ONLY public.cours_planifie DROP CONSTRAINT IF EXISTS fk6c7kif5ssn61eikqp9k7adg23;
ALTER TABLE IF EXISTS ONLY public.cours_prerequis DROP CONSTRAINT IF EXISTS fk5nxbonyou9ejnupq89wmuvlnd;
ALTER TABLE IF EXISTS ONLY public.inscription_cours DROP CONSTRAINT IF EXISTS fk1v1wlmkhgqy9b0heokl2xwbil;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.rythme DROP CONSTRAINT IF EXISTS uko5c4f5wqyc0wwxidyj554mshw;
ALTER TABLE IF EXISTS ONLY public.filiere DROP CONSTRAINT IF EXISTS ukmnlcjeavleh7f8hbcfe0kb4di;
ALTER TABLE IF EXISTS ONLY public.cursus DROP CONSTRAINT IF EXISTS ukimkhxbqlegxlocvies85709hw;
ALTER TABLE IF EXISTS ONLY public.inscription_cours DROP CONSTRAINT IF EXISTS ukf2lbnoljrnx7n4501735jy84c;
ALTER TABLE IF EXISTS ONLY public.invitation_token DROP CONSTRAINT IF EXISTS ukdidjhk2d2etgia8bvuw7pix8n;
ALTER TABLE IF EXISTS ONLY public.cursus_cours DROP CONSTRAINT IF EXISTS ukc0vxe2fecea9qon4liikl604u;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS uk6dotkott2kjsp8vw4d0m25fb7;
ALTER TABLE IF EXISTS ONLY public.rythme DROP CONSTRAINT IF EXISTS rythme_pkey;
ALTER TABLE IF EXISTS ONLY public.promotion DROP CONSTRAINT IF EXISTS promotion_pkey;
ALTER TABLE IF EXISTS ONLY public.promotion_cours DROP CONSTRAINT IF EXISTS promotion_cours_pkey;
ALTER TABLE IF EXISTS ONLY public.invitation_token DROP CONSTRAINT IF EXISTS invitation_token_pkey;
ALTER TABLE IF EXISTS ONLY public.inscription_cours DROP CONSTRAINT IF EXISTS inscription_cours_pkey;
ALTER TABLE IF EXISTS ONLY public.filiere DROP CONSTRAINT IF EXISTS filiere_pkey;
ALTER TABLE IF EXISTS ONLY public.cursus DROP CONSTRAINT IF EXISTS cursus_pkey;
ALTER TABLE IF EXISTS ONLY public.cursus_cours DROP CONSTRAINT IF EXISTS cursus_cours_pkey;
ALTER TABLE IF EXISTS ONLY public.cours_prerequis DROP CONSTRAINT IF EXISTS cours_prerequis_pkey;
ALTER TABLE IF EXISTS ONLY public.cours_planifie DROP CONSTRAINT IF EXISTS cours_planifie_pkey;
ALTER TABLE IF EXISTS ONLY public.cours DROP CONSTRAINT IF EXISTS cours_pkey;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.rythme;
DROP TABLE IF EXISTS public.promotion_cours;
DROP TABLE IF EXISTS public.promotion;
DROP TABLE IF EXISTS public.invitation_token;
DROP TABLE IF EXISTS public.inscription_cours;
DROP TABLE IF EXISTS public.filiere;
DROP TABLE IF EXISTS public.cursus_cours;
DROP TABLE IF EXISTS public.cursus;
DROP TABLE IF EXISTS public.cours_prerequis;
DROP TABLE IF EXISTS public.cours_planifie;
DROP TABLE IF EXISTS public.cours_formateurs;
DROP TABLE IF EXISTS public.cours;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cours; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cours (
    id bigint NOT NULL,
    name character varying(255),
    cursus_id bigint,
    duree_jours integer
);


ALTER TABLE public.cours OWNER TO postgres;

--
-- Name: cours_formateurs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cours_formateurs (
    cours_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.cours_formateurs OWNER TO postgres;

--
-- Name: cours_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.cours ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cours_planifie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cours_planifie (
    id bigint NOT NULL,
    date_debut date,
    date_fin date,
    ordre integer NOT NULL,
    salle character varying(255),
    statut character varying(255),
    cours_id bigint NOT NULL,
    formateur_id bigint,
    promotion_id bigint,
    CONSTRAINT cours_planifie_statut_check CHECK (((statut)::text = ANY ((ARRAY['PLANIFIE'::character varying, 'EN_COURS'::character varying, 'TERMINE'::character varying])::text[])))
);


ALTER TABLE public.cours_planifie OWNER TO postgres;

--
-- Name: cours_planifie_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.cours_planifie ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cours_planifie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cours_prerequis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cours_prerequis (
    cours_id bigint NOT NULL,
    prerequis_id bigint NOT NULL
);


ALTER TABLE public.cours_prerequis OWNER TO postgres;

--
-- Name: cursus; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cursus (
    id bigint NOT NULL,
    name character varying(255),
    filiere_id bigint
);


ALTER TABLE public.cursus OWNER TO postgres;

--
-- Name: cursus_cours; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cursus_cours (
    id bigint NOT NULL,
    ordre integer NOT NULL,
    cours_id bigint,
    cursus_id bigint
);


ALTER TABLE public.cursus_cours OWNER TO postgres;

--
-- Name: cursus_cours_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.cursus_cours ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cursus_cours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cursus_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.cursus ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cursus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: filiere; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filiere (
    id bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.filiere OWNER TO postgres;

--
-- Name: filiere_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.filiere ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.filiere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: inscription_cours; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inscription_cours (
    id bigint NOT NULL,
    date_inscription date,
    cours_planifie_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.inscription_cours OWNER TO postgres;

--
-- Name: inscription_cours_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.inscription_cours ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.inscription_cours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: invitation_token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invitation_token (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    expiration_date timestamp(6) without time zone,
    role character varying(255),
    token character varying(255) NOT NULL,
    used boolean NOT NULL,
    CONSTRAINT invitation_token_role_check CHECK (((role)::text = ANY ((ARRAY['ETUDIANT'::character varying, 'REFERENTE_ADMINISTRATIVE'::character varying, 'ADMINISTRATEUR'::character varying, 'FORMATEUR'::character varying])::text[])))
);


ALTER TABLE public.invitation_token OWNER TO postgres;

--
-- Name: invitation_token_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.invitation_token ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.invitation_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: promotion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.promotion (
    id bigint NOT NULL,
    date_debut date,
    name character varying(255) NOT NULL,
    cursus_id bigint
);


ALTER TABLE public.promotion OWNER TO postgres;

--
-- Name: promotion_cours; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.promotion_cours (
    id bigint NOT NULL,
    date_debut date,
    date_fin date,
    ordre integer NOT NULL,
    statut character varying(255),
    cours_id bigint NOT NULL,
    promotion_id bigint NOT NULL,
    CONSTRAINT promotion_cours_statut_check CHECK (((statut)::text = ANY ((ARRAY['PLANIFIE'::character varying, 'EN_COURS'::character varying, 'TERMINE'::character varying])::text[])))
);


ALTER TABLE public.promotion_cours OWNER TO postgres;

--
-- Name: promotion_cours_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.promotion_cours ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.promotion_cours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: promotion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.promotion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.promotion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: rythme; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rythme (
    id bigint NOT NULL,
    semaines_centre integer NOT NULL,
    semaines_entreprise integer NOT NULL,
    promotion_id bigint
);


ALTER TABLE public.rythme OWNER TO postgres;

--
-- Name: rythme_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.rythme ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.rythme_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    uid bigint NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    password character varying(255),
    role character varying(255),
    enabled boolean DEFAULT true NOT NULL,
    promotion_id bigint,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ETUDIANT'::character varying, 'REFERENTE_ADMINISTRATIVE'::character varying, 'ADMINISTRATEUR'::character varying, 'FORMATEUR'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_uid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN uid ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: cours; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cours (id, name, cursus_id, duree_jours) FROM stdin;
8	Algorithmique / Pseudo-Code	\N	5
9	Initiation à la Programmation / Java	\N	5
10	Web Client / HTML & CSS	\N	10
11	JavaScript initiation	\N	10
12	Projet Web / HTML & CSS + JS	\N	5
13	Programmation Orientée Objet / Java	\N	20
14	Langage SQL / SQL Server	\N	10
15	Notions Complémentaires / Java SE	\N	10
16	Développement Web côté Serveur (Back-End) / Java Spring Boot	\N	30
17	Projet Web / Java Spring Boot	\N	20
18	Analyse et Conception / Oracle Data Modeler	\N	15
19	JavaScript avancé + initiation Framework JS / Angular	\N	10
20	Développement Web côté Serveur avec JavaScript / Node.js et NoSQL	\N	10
21	Développement Web côté Serveur (Back-End) / PHP	\N	10
22	Développement Web côté Serveur (Back-End) / Symfony	\N	20
23	Projet Web / Symfony	\N	20
24	CMS / WordPress	\N	5
25	CMS / WordPress + Projet Final	\N	5
26	Algorithmique + Initiation à la Programmation / Java	\N	5
27	SQL avancé / Transact SQL et Sécurité	\N	5
28	Gestion de projet et Communication	\N	5
29	Java Frameworks - API Web (Spring Security, ORM, …)	\N	10
30	Angular avancé / Angular	\N	5
31	Projet Fullstack - Web / Java Spring Boot + Angular	\N	10
32	Technologie Cross-Platform / Flutter	\N	5
33	DevOps - Infrastructure et déploiement d’applications	\N	5
34	Intelligence Artificielle / Python	\N	5
35	IA / Python + Projet Final	\N	5
36	Lead Pentester	\N	10
37	Techniques de hacking avancées	\N	5
38	Test Intrusion avec Python (+ Examen Bloc)	\N	5
39	Cyberdéfense	\N	5
40	SOC Security Manager (+ Examen Bloc)	\N	5
41	Investigation Numérique - Réseau et Windows	\N	5
42	Fondamentaux de l'Analyse de Malware (+ Examen Bloc)	\N	5
43	Gestion de projets et juridique	\N	5
44	Gestion des risques SI avec ISO 27005 & EBIOS 2010 / RM	\N	5
45	Intégration SMSI avec ISO 27001	\N	5
46	Plan de continuité (PCA) avec ISO 22301	\N	5
47	DevOps Security Manager (+ Examen Bloc)	\N	5
48	Wargame	\N	5
49	Préparation Examen	\N	5
50	Livraison Questions CTF	\N	5
51	Livraison Mémoire / Correction	\N	5
52	Livraison Finale Mémoire	\N	5
53	Introduction à l'architecture logicielle, principes et modélisation UML	\N	5
54	Design patterns et conception logicielle avancée	\N	5
55	Techniques avancées d'analyse des besoins et retranscription	\N	5
56	Management et documentation de projet numérique en agilité	\N	5
57	CI / CD / Automatisation des tests avec l'IA et documentation	\N	5
58	DevSecOps - Sécuriser les projets Cloud	\N	5
59	Programmation avancée en Python et JS pour la Conception et la Sécurité Fullstack	\N	5
60	Sécurisation et Optimisation des Systèmes (BDD, Application, …)	\N	5
61	Cloud Computing – Fondamentaux AWS	\N	5
62	Infrastructure as Code avec Terraform	\N	5
63	Automatiser la gestion des serveurs avec Ansible	\N	5
64	Sécurisation et FinOps des environnements Cloud	\N	5
65	Posture du manager d'équipe, de projet et du leader technique	\N	5
66	Optimisation des architectures multi-cloud et hybrid cloud	\N	5
67	Monitoring et observabilité des systèmes	\N	5
68	Réseaux et connectivité dans le SI	\N	5
69	Introduction à la BI et aux outils ETL	\N	5
70	Conception des entrepôts de données et flux ETL avancés	\N	5
71	Processus décisionnels et modèles de données	\N	5
72	Technologies RPA (probots, knowbots et chatbots)	\N	5
73	Contrat de prestation / Négociation / Organisation d'une DSI	\N	5
74	Introduction au Big Data	\N	5
75	Traitement des données en temps réel	\N	5
76	Déploiement et gestion des modèles ML avec MLOps	\N	5
77	Automatisation des pipelines ML	\N	5
78	Projet : Conception et développement de la solution prédictive	\N	5
79	Projet : Configuration de l'infra Cloud et Data, déploiement, tests et documentation de la solution	\N	5
80	Smart contracts et blockchain / Développement sécurisé d’Application	\N	5
81	Veille technologique, conformité de la solution et numérique responsable	\N	5
82	Edge computing et informatique quantique	\N	5
83	Anglais	\N	5
84	Entrainement Anglais via Plateforme	\N	5
85	TOEIC - Listening and Reading	\N	5
\.


--
-- Data for Name: cours_formateurs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cours_formateurs (cours_id, user_id) FROM stdin;
\.


--
-- Data for Name: cours_planifie; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cours_planifie (id, date_debut, date_fin, ordre, salle, statut, cours_id, formateur_id, promotion_id) FROM stdin;
\.


--
-- Data for Name: cours_prerequis; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cours_prerequis (cours_id, prerequis_id) FROM stdin;
11	10
13	11
14	13
15	14
16	15
17	16
18	17
19	18
20	19
21	20
22	21
23	22
27	23
28	27
29	28
30	29
31	30
32	31
33	32
34	33
35	34
37	36
38	37
39	38
40	39
41	40
42	41
43	42
44	43
45	44
46	45
47	46
48	47
49	48
50	49
51	50
52	51
54	53
55	54
56	55
57	56
58	57
59	58
60	59
61	60
62	61
63	62
64	63
65	64
66	65
67	66
68	67
69	68
70	69
71	70
72	71
73	72
74	73
75	74
76	75
77	76
78	77
79	78
80	79
81	80
82	81
83	82
84	83
85	84
\.


--
-- Data for Name: cursus; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cursus (id, name, filiere_id) FROM stdin;
5	Développeur web et web mobile	4
6	CDA	4
7	ESD	5
8	EADL	4
\.


--
-- Data for Name: cursus_cours; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cursus_cours (id, ordre, cours_id, cursus_id) FROM stdin;
42	4	14	6
43	5	15	6
44	6	16	6
45	7	17	6
46	8	18	6
47	9	19	6
48	10	20	6
49	11	21	6
50	12	22	6
51	13	23	6
52	14	27	6
53	15	28	6
54	16	29	6
55	17	30	6
56	18	31	6
57	19	32	6
58	20	33	6
59	21	34	6
60	22	35	6
61	0	36	7
62	1	37	7
63	2	38	7
64	3	39	7
65	4	40	7
66	5	41	7
67	6	42	7
68	7	43	7
69	8	44	7
70	9	45	7
71	10	46	7
72	11	47	7
73	12	48	7
74	13	49	7
75	14	50	7
76	15	51	7
77	16	52	7
78	0	53	8
79	1	54	8
80	2	55	8
81	3	56	8
82	4	57	8
83	5	58	8
84	6	59	8
85	7	60	8
86	8	61	8
24	4	12	5
25	5	13	5
26	6	14	5
27	7	15	5
28	8	16	5
29	9	17	5
30	10	18	5
31	11	19	5
32	12	20	5
33	13	21	5
34	14	22	5
35	15	23	5
36	16	24	5
37	17	25	5
87	9	62	8
88	10	63	8
89	11	64	8
90	12	65	8
91	13	66	8
92	14	67	8
93	15	68	8
94	16	69	8
95	17	70	8
96	18	71	8
97	19	72	8
98	20	73	8
99	21	74	8
100	22	75	8
101	23	76	8
102	24	77	8
103	25	78	8
104	26	79	8
105	27	80	8
106	28	81	8
107	29	82	8
108	30	83	8
109	31	84	8
110	32	85	8
21	1	9	5
20	0	8	5
23	3	11	5
22	2	10	5
38	3	26	6
40	1	11	6
41	2	13	6
39	0	10	6
\.


--
-- Data for Name: filiere; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filiere (id, name) FROM stdin;
4	Développement
5	Systèmes & Réseau
\.


--
-- Data for Name: inscription_cours; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inscription_cours (id, date_inscription, cours_planifie_id, user_id) FROM stdin;
\.


--
-- Data for Name: invitation_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.invitation_token (id, email, expiration_date, role, token, used) FROM stdin;
1	tomydacalor@hotmail.com	2026-06-09 16:36:58.669362	ADMINISTRATEUR	28844a51-2eb5-497a-a923-24c00bd34990	t
3	test@est.com	2026-06-09 16:48:11.890697	ADMINISTRATEUR	19237559-5462-4c48-a0e7-56cf966f2960	t
4	test@est.com	2026-06-09 16:54:18.423022	ADMINISTRATEUR	2a37e653-ec28-45c2-b4d3-0b1bd14efc95	t
5	dacalor.tomy@gmail.com	2026-06-09 23:07:18.862348	REFERENTE_ADMINISTRATIVE	15d1f5a4-30aa-4397-9e4c-d739d46c7778	t
6	tomydacalor@hotmail.com	2026-06-09 23:08:28.96208	ADMINISTRATEUR	aa4b1221-5d8f-4940-bcb9-d369c4428469	f
2	tomydacalor@hotmail.com	2026-06-09 16:47:45.834347	ADMINISTRATEUR	8b172828-b9aa-4ac4-9401-d4dadc9d8f13	t
\.


--
-- Data for Name: promotion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.promotion (id, date_debut, name, cursus_id) FROM stdin;
\.


--
-- Data for Name: promotion_cours; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.promotion_cours (id, date_debut, date_fin, ordre, statut, cours_id, promotion_id) FROM stdin;
\.


--
-- Data for Name: rythme; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rythme (id, semaines_centre, semaines_entreprise, promotion_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (uid, email, first_name, last_name, password, role, enabled, promotion_id) FROM stdin;
1	admin@admin.com	Admin	ADMINISTRATEUR	$2a$10$tNwZNbJxjVYLcHMKWrn1M.WovShiAsZL1RQcgRnDXYIPR.RCIFHw6	ADMINISTRATEUR	t	\N
11	eleve1@eni.fr	Eleve	Un	$2a$10$0PtzIhAtoooWRxupulnZ1uVmtATpOOHSBWR4gA7V.NPYZFOBC8oIm	ETUDIANT	t	\N
7	dacalor.tomy@gmail.com	Tomy	DACALOR	$2a$10$kzv/6PwfzwQq6XHRCqRU1.t6oi5BqiaPq7.BlXJ1sF6cLIz6j0SO6	REFERENTE_ADMINISTRATIVE	t	\N
8	ref@ref.com	jean	Dupont	$2a$10$xi7h4Xwp529sPEMNhYhz7.iiWld7Th4MguGKp6EJmAviMLojzrVDq	REFERENTE_ADMINISTRATIVE	t	\N
9	ref@eni.fr	Refe	Rente	$2a$10$pH5hwHgbN1ceWvjJBvrX7evWxmUFHmMugO99HmFbiRnfm3SUeYwi6	REFERENTE_ADMINISTRATIVE	t	\N
10	formateur1@eni.fr	Form	Ateur	$2a$10$1fxZFJeVUq/lRICCUVbkYOFxm5e9bWyVxqqGTjeC8.SUjcOll9roi	FORMATEUR	t	\N
12	eleve2@eni.fr	Eleve	Deux	$2a$10$YlCY2WfBsonobFDk1lAu5uETjSheBUa4MlJnKQH7zMVH/n3hQbLN2	ETUDIANT	t	\N
13	eleve@eleve.com	Eleve	eleve	$2a$10$kUe5tPze1fbXkIQJtS6l.eciKfM/GAeQFeK1xmelU6OFvpJViqTBK	ETUDIANT	t	\N
\.


--
-- Name: cours_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cours_id_seq', 85, true);


--
-- Name: cours_planifie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cours_planifie_id_seq', 28, true);


--
-- Name: cursus_cours_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cursus_cours_id_seq', 110, true);


--
-- Name: cursus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cursus_id_seq', 8, true);


--
-- Name: filiere_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.filiere_id_seq', 5, true);


--
-- Name: inscription_cours_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inscription_cours_id_seq', 3, true);


--
-- Name: invitation_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.invitation_token_id_seq', 6, true);


--
-- Name: promotion_cours_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.promotion_cours_id_seq', 20, true);


--
-- Name: promotion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.promotion_id_seq', 11, true);


--
-- Name: rythme_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rythme_id_seq', 5, true);


--
-- Name: users_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_uid_seq', 13, true);


--
-- Name: cours cours_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours
    ADD CONSTRAINT cours_pkey PRIMARY KEY (id);


--
-- Name: cours_planifie cours_planifie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_planifie
    ADD CONSTRAINT cours_planifie_pkey PRIMARY KEY (id);


--
-- Name: cours_prerequis cours_prerequis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_prerequis
    ADD CONSTRAINT cours_prerequis_pkey PRIMARY KEY (cours_id, prerequis_id);


--
-- Name: cursus_cours cursus_cours_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus_cours
    ADD CONSTRAINT cursus_cours_pkey PRIMARY KEY (id);


--
-- Name: cursus cursus_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus
    ADD CONSTRAINT cursus_pkey PRIMARY KEY (id);


--
-- Name: filiere filiere_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filiere
    ADD CONSTRAINT filiere_pkey PRIMARY KEY (id);


--
-- Name: inscription_cours inscription_cours_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inscription_cours
    ADD CONSTRAINT inscription_cours_pkey PRIMARY KEY (id);


--
-- Name: invitation_token invitation_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invitation_token
    ADD CONSTRAINT invitation_token_pkey PRIMARY KEY (id);


--
-- Name: promotion_cours promotion_cours_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion_cours
    ADD CONSTRAINT promotion_cours_pkey PRIMARY KEY (id);


--
-- Name: promotion promotion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion
    ADD CONSTRAINT promotion_pkey PRIMARY KEY (id);


--
-- Name: rythme rythme_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rythme
    ADD CONSTRAINT rythme_pkey PRIMARY KEY (id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: cursus_cours ukc0vxe2fecea9qon4liikl604u; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus_cours
    ADD CONSTRAINT ukc0vxe2fecea9qon4liikl604u UNIQUE (cursus_id, cours_id);


--
-- Name: invitation_token ukdidjhk2d2etgia8bvuw7pix8n; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invitation_token
    ADD CONSTRAINT ukdidjhk2d2etgia8bvuw7pix8n UNIQUE (token);


--
-- Name: inscription_cours ukf2lbnoljrnx7n4501735jy84c; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inscription_cours
    ADD CONSTRAINT ukf2lbnoljrnx7n4501735jy84c UNIQUE (user_id, cours_planifie_id);


--
-- Name: cursus ukimkhxbqlegxlocvies85709hw; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus
    ADD CONSTRAINT ukimkhxbqlegxlocvies85709hw UNIQUE (name);


--
-- Name: filiere ukmnlcjeavleh7f8hbcfe0kb4di; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filiere
    ADD CONSTRAINT ukmnlcjeavleh7f8hbcfe0kb4di UNIQUE (name);


--
-- Name: rythme uko5c4f5wqyc0wwxidyj554mshw; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rythme
    ADD CONSTRAINT uko5c4f5wqyc0wwxidyj554mshw UNIQUE (promotion_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (uid);


--
-- Name: inscription_cours fk1v1wlmkhgqy9b0heokl2xwbil; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inscription_cours
    ADD CONSTRAINT fk1v1wlmkhgqy9b0heokl2xwbil FOREIGN KEY (cours_planifie_id) REFERENCES public.cours_planifie(id);


--
-- Name: cours_prerequis fk5nxbonyou9ejnupq89wmuvlnd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_prerequis
    ADD CONSTRAINT fk5nxbonyou9ejnupq89wmuvlnd FOREIGN KEY (prerequis_id) REFERENCES public.cours(id);


--
-- Name: cours_planifie fk6c7kif5ssn61eikqp9k7adg23; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_planifie
    ADD CONSTRAINT fk6c7kif5ssn61eikqp9k7adg23 FOREIGN KEY (formateur_id) REFERENCES public.users(uid);


--
-- Name: cursus_cours fk6dlnqb0u7il9qxisik7vce4qj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus_cours
    ADD CONSTRAINT fk6dlnqb0u7il9qxisik7vce4qj FOREIGN KEY (cours_id) REFERENCES public.cours(id);


--
-- Name: cours_formateurs fk9eqp2hqblnovqmfbecuhvbkjm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_formateurs
    ADD CONSTRAINT fk9eqp2hqblnovqmfbecuhvbkjm FOREIGN KEY (user_id) REFERENCES public.users(uid);


--
-- Name: users fk9ximuq2tyddafxqlv006h4e29; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk9ximuq2tyddafxqlv006h4e29 FOREIGN KEY (promotion_id) REFERENCES public.promotion(id);


--
-- Name: promotion_cours fke4hvytgku0ixrlx66ynjclp65; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion_cours
    ADD CONSTRAINT fke4hvytgku0ixrlx66ynjclp65 FOREIGN KEY (promotion_id) REFERENCES public.promotion(id);


--
-- Name: cours_prerequis fkeoyeyjg4jqaloy9lm1jl969f6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_prerequis
    ADD CONSTRAINT fkeoyeyjg4jqaloy9lm1jl969f6 FOREIGN KEY (cours_id) REFERENCES public.cours(id);


--
-- Name: cours_planifie fkih4sweo6debkljw4j6jmymxtu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_planifie
    ADD CONSTRAINT fkih4sweo6debkljw4j6jmymxtu FOREIGN KEY (cours_id) REFERENCES public.cours(id);


--
-- Name: promotion fkij3ht1gimy6e0o66m5bhtmg7t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion
    ADD CONSTRAINT fkij3ht1gimy6e0o66m5bhtmg7t FOREIGN KEY (cursus_id) REFERENCES public.cursus(id);


--
-- Name: cours_formateurs fkij72rjkjgaj5sovrxvv07aeka; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_formateurs
    ADD CONSTRAINT fkij72rjkjgaj5sovrxvv07aeka FOREIGN KEY (cours_id) REFERENCES public.cours(id);


--
-- Name: cursus_cours fklpj3xquqsobf77kcc6plfu0sh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus_cours
    ADD CONSTRAINT fklpj3xquqsobf77kcc6plfu0sh FOREIGN KEY (cursus_id) REFERENCES public.cursus(id);


--
-- Name: cours_planifie fkn9m3a5ef4lq5j0ah4hf9npkm4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours_planifie
    ADD CONSTRAINT fkn9m3a5ef4lq5j0ah4hf9npkm4 FOREIGN KEY (promotion_id) REFERENCES public.promotion(id);


--
-- Name: promotion_cours fkof99okhawxx4r0kih8gevy7l4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion_cours
    ADD CONSTRAINT fkof99okhawxx4r0kih8gevy7l4 FOREIGN KEY (cours_id) REFERENCES public.cours(id);


--
-- Name: rythme fkplrj2pwsxg1miql9lg2kkokj7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rythme
    ADD CONSTRAINT fkplrj2pwsxg1miql9lg2kkokj7 FOREIGN KEY (promotion_id) REFERENCES public.promotion(id);


--
-- Name: cours fkpron8fnvmqcw5nkrrn1576m3q; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cours
    ADD CONSTRAINT fkpron8fnvmqcw5nkrrn1576m3q FOREIGN KEY (cursus_id) REFERENCES public.cursus(id);


--
-- Name: inscription_cours fkrgbjflyb7d08536o4iwwyo6hu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inscription_cours
    ADD CONSTRAINT fkrgbjflyb7d08536o4iwwyo6hu FOREIGN KEY (user_id) REFERENCES public.users(uid);


--
-- Name: cursus fktfpoco4anexe2mx4fycanxh9m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cursus
    ADD CONSTRAINT fktfpoco4anexe2mx4fycanxh9m FOREIGN KEY (filiere_id) REFERENCES public.filiere(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 1OFxcyqLw3zEv8MHLU3QEv810K3AWAPkxlDzE4kutwb6cjnQnkfuk30gFOdVA0u

