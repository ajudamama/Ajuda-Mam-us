package com.example.data.db

import com.example.data.model.*

object InitialData {

    fun getDefaultChildren(): List<ChildEntity> = emptyList()

    fun getDefaultVaccineRecords(): List<VaccineRecordEntity> = emptyList()

    fun getDefaultVaccineCatalog(): List<VaccineCatalogEntity> = listOf(
        VaccineCatalogEntity(
            id = 1, name = "BCG", emoji = "🛡️",
            targetDisease = "Tuberculose grave e meningite tuberculosa",
            dosesCount = "1", scheduledAges = "Ao nascer",
            description = "Protege recém-nascidos contra as formas mais graves e disseminadas de tuberculose.",
            route = "Intradérmica"
        ),
        VaccineCatalogEntity(
            id = 2, name = "Pentavalente", emoji = "💉",
            targetDisease = "Difteria, Tétano, Coqueluche, Hepatite B e Hib",
            dosesCount = "3", scheduledAges = "6, 10 e 14 semanas",
            description = "Combinação de 5 vacinas essenciais em dose única combinada para imunização infantil primária.",
            route = "Intramuscular profunda"
        ),
        VaccineCatalogEntity(
            id = 3, name = "VOP (Poliomielite Oral)", emoji = "🛡️",
            targetDisease = "Poliomielite (Paralisia infantil)",
            dosesCount = "3 + reforços", scheduledAges = "Ao nascer, 6, 10, 14 sem e 24 meses",
            description = "Vacina viva atenuada administrada por via oral em gotas para imunidade mucosal contra poliovírus.",
            route = "Oral (gotas)"
        ),
        VaccineCatalogEntity(
            id = 4, name = "VIP (Poliomielite Inactivada)", emoji = "💉",
            targetDisease = "Poliomielite",
            dosesCount = "2", scheduledAges = "6 e 14 semanas",
            description = "Vacina inactivada administrada por injecção intramuscular como reforço de segurança.",
            route = "Intramuscular"
        ),
        VaccineCatalogEntity(
            id = 5, name = "Pneumo (Pneumocócica)", emoji = "🫁",
            targetDisease = "Pneumonia, Meningite e Sepse por Pneumococo",
            dosesCount = "3", scheduledAges = "6, 10 e 14 semanas",
            description = "Protege contra infecções bacterianas graves do trato respiratório e sistema nervoso central.",
            route = "Intramuscular"
        ),
        VaccineCatalogEntity(
            id = 6, name = "Rotavírus", emoji = "🦠",
            targetDisease = "Diarreia grave por Rotavírus e desidratação infantil",
            dosesCount = "2", scheduledAges = "6 e 10 semanas",
            description = "Protege contra gastroenterite viral aguda grave em lactentes e crianças pequenas.",
            route = "Oral (líquido)"
        ),
        VaccineCatalogEntity(
            id = 7, name = "Sarampo", emoji = "🔴",
            targetDisease = "Sarampo e complicações associadas",
            dosesCount = "2", scheduledAges = "9 e 18 meses",
            description = "Protege contra o vírus do sarampo, doença altamente infecciosa com risco de pneumonia e encefalite.",
            route = "Subcutânea"
        ),
        VaccineCatalogEntity(
            id = 8, name = "Febre Amarela", emoji = "🦟",
            targetDisease = "Febre amarela endémica transmitida por mosquito",
            dosesCount = "1", scheduledAges = "9 meses",
            description = "Confere imunidade duradoura contra o vírus da febre amarela em zonas recomendadas.",
            route = "Subcutânea"
        )
    )

    fun getDefaultFacilities(): List<HealthFacilityEntity> = listOf(
        HealthFacilityEntity(
            id = 1, name = "US Polana", type = "Unidade Sanitária",
            distance = "1.2 km", address = "Bairro da Polana, Maputo",
            operatingHours = "07:30 - 15:30", phone = "+258 21 461 234",
            services = "Vacinação,Consultas Pediátricas,Maternidade,Planeamento Familiar",
            region = "Maputo", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        ),
        HealthFacilityEntity(
            id = 2, name = "HC José Macamo", type = "Hospital Central",
            distance = "3.8 km", address = "Av. Eduardo Mondlane, Maputo",
            operatingHours = "24 Horas", phone = "+258 21 491 000",
            services = "Vacinação,Urgências,Especialidades,Internamento",
            region = "Maputo", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        ),
        HealthFacilityEntity(
            id = 3, name = "US da Sommerschield", type = "Unidade Sanitária",
            distance = "2.5 km", address = "Sommerschield, Maputo",
            operatingHours = "07:30 - 16:00", phone = "+258 21 492 567",
            services = "Vacinação,Saúde Materno-Infantil,Nutrição",
            region = "Maputo", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        ),
        HealthFacilityEntity(
            id = 4, name = "US de Marracuene", type = "Centro de Saúde",
            distance = "28 km", address = "Vila de Marracuene, Província de Maputo",
            operatingHours = "07:30 - 15:00", phone = "+258 21 960 123",
            services = "Vacinação,Consulta Pré-natal,Farmácia",
            region = "Marracuene", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        ),
        HealthFacilityEntity(
            id = 5, name = "US da Matola", type = "Centro de Saúde",
            distance = "15 km", address = "Matola 'A', Maputo",
            operatingHours = "07:00 - 16:30", phone = "+258 21 720 444",
            services = "Vacinação,Triagem Infantil,Laboratório",
            region = "Matola", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        ),
        HealthFacilityEntity(
            id = 6, name = "US de Boane", type = "Centro de Saúde",
            distance = "35 km", address = "Boane Bairro Central",
            operatingHours = "07:30 - 15:00", phone = "+258 21 770 890",
            services = "Vacinação,Cuidados Gerais,Posto Móvel",
            region = "Boane", coveragePct = 0, registeredChildrenCount = 0, status = "Activa"
        )
    )

    fun getDefaultHealthWorkers(): List<HealthWorkerEntity> = listOf(
        HealthWorkerEntity(
            id = 1, name = "Dr. Carlos Tembe", title = "Enfermeiro de Saúde Materno-Infantil",
            facilityName = "US Polana", avatarInitials = "CT", dosesCount = 0, childrenCount = 0, isActive = true
        ),
        HealthWorkerEntity(
            id = 2, name = "Enf. Lúcia Mondlane", title = "Enfermeira de Vacinação PTV",
            facilityName = "US Polana", avatarInitials = "LM", dosesCount = 0, childrenCount = 0, isActive = true
        ),
        HealthWorkerEntity(
            id = 3, name = "Dr. Alberto Matsinhe", title = "Médico Pediatra Chefe",
            facilityName = "HC José Macamo", avatarInitials = "AM", dosesCount = 0, childrenCount = 0, isActive = true
        )
    )

    fun getDefaultStock(): List<VaccineStockEntity> = listOf(
        VaccineStockEntity(id = 1, vaccineName = "BCG", lotNumber = "BCG-2026-04", quantity = 180, expirationDate = "Mar 2027", status = "OK"),
        VaccineStockEntity(id = 2, vaccineName = "Pentavalente", lotNumber = "PT-2026-11", quantity = 420, expirationDate = "Set 2026", status = "A vencer"),
        VaccineStockEntity(id = 3, vaccineName = "VOP", lotNumber = "VOP-2026-08", quantity = 650, expirationDate = "Dez 2027", status = "OK"),
        VaccineStockEntity(id = 4, vaccineName = "VIP", lotNumber = "VIP-2026-01", quantity = 290, expirationDate = "Out 2027", status = "OK"),
        VaccineStockEntity(id = 5, vaccineName = "Pneumo", lotNumber = "PN-2026-03", quantity = 380, expirationDate = "Jan 2028", status = "OK"),
        VaccineStockEntity(id = 6, vaccineName = "Rotavírus", lotNumber = "RV-2026-05", quantity = 210, expirationDate = "Nov 2027", status = "OK"),
        VaccineStockEntity(id = 7, vaccineName = "Sarampo", lotNumber = "SR-2026-02", quantity = 310, expirationDate = "Jun 2027", status = "OK"),
        VaccineStockEntity(id = 8, vaccineName = "Febre Amarela", lotNumber = "FA-2026-06", quantity = 85, expirationDate = "Nov 2027", status = "OK")
    )

    fun getDefaultAuditLogs(): List<AuditLogEntity> = listOf(
        AuditLogEntity(
            id = 1,
            title = "Sistema VacciTrack Inicializado",
            subtitle = "Contabilização em tempo real pronta. Registe novos utentes e vacinas.",
            category = "Sistema",
            timeAgo = "Agora mesmo"
        )
    )

    fun getDefaultUserAccounts(): List<UserAccountEntity> = listOf(
        UserAccountEntity(id = 1, name = "Ana Macuácua", emailOrPhone = "+258 84 123 4567", password = "123456", role = "parent", childrenCount = 0, status = "Activo"),
        UserAccountEntity(id = 2, name = "Marta Cossa", emailOrPhone = "+258 84 765 4321", password = "123456", role = "parent", childrenCount = 0, status = "Activo"),
        UserAccountEntity(id = 3, name = "Joana Sitoe", emailOrPhone = "+258 84 999 1122", password = "123456", role = "parent", childrenCount = 0, status = "Activo"),
        UserAccountEntity(id = 4, name = "Paulo Nhaca", emailOrPhone = "+258 84 334 5566", password = "123456", role = "parent", childrenCount = 0, status = "Activo"),
        UserAccountEntity(id = 5, name = "Dr. Carlos Tembe", emailOrPhone = "carlos.tembe@saude.gov.mz", password = "123456", role = "pro", parentType = "Profissional", childrenCount = 0, status = "Activo"),
        UserAccountEntity(id = 6, name = "Enf. Lúcia Mondlane", emailOrPhone = "lucia.mondlane@saude.gov.mz", password = "123456", role = "pro", parentType = "Profissional", childrenCount = 0, status = "Activo"),
        UserAccountEntity(
            id = 7,
            name = "Admin Principal (Master)",
            emailOrPhone = "cepsgadmin@jorgito2026",
            email = "cepsgadmin@jorgito2026",
            password = "hhJorge*414life",
            role = "admin",
            parentType = "Admin Principal",
            childrenCount = 0,
            status = "Activo"
        )
    )
}
