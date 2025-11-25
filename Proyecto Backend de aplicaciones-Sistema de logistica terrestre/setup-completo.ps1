# =====================================================
# Script de Setup Completo del Sistema TPI
# =====================================================
# Este script:
# 1. Elimina contenedores y volúmenes existentes
# 2. Rebuildea todas las imágenes desde cero (10 minutos)
# 3. Reinicia la BD a 0 con init-db.sql
# 4. Configura Keycloak automáticamente
# 5. Obtiene tokens para testing
# =====================================================

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SETUP COMPLETO DEL SISTEMA TPI" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# =====================================================
# PASO 1: Eliminar contenedores y volúmenes existentes
# =====================================================
Write-Host "1. Eliminando contenedores y volúmenes existentes..." -ForegroundColor Yellow

try {
    docker-compose down -v 2>&1 | Out-Null
    Write-Host "   Contenedores y volúmenes eliminados" -ForegroundColor Green
} catch {
    Write-Host "   No había contenedores para eliminar" -ForegroundColor Gray
}

Write-Host ""

# =====================================================
# PASO 2: Rebuildear todas las imágenes desde cero
# =====================================================
Write-Host "2. Rebuildando imágenes Docker desde cero..." -ForegroundColor Yellow
Write-Host "   Esto puede tomar aproximadamente 10 minutos..." -ForegroundColor Gray
Write-Host ""

$buildStartTime = Get-Date

try {
    # Rebuildear sin cache
    docker-compose build --no-cache
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error al rebuildear las imágenes"
    }
    
    $buildEndTime = Get-Date
    $buildDuration = $buildEndTime - $buildStartTime
    $buildMinutes = [Math]::Round($buildDuration.TotalMinutes, 1)
    
    Write-Host ""
    Write-Host "   Build completado en $buildMinutes minutos" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "ERROR: No se pudieron rebuildear las imágenes Docker" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""

# =====================================================
# PASO 3: Iniciar servicios (esto ejecuta init-db.sql)
# =====================================================
Write-Host "3. Iniciando servicios y reiniciando BD a 0..." -ForegroundColor Yellow
Write-Host "   La BD se reiniciará automáticamente con init-db.sql..." -ForegroundColor Gray

try {
    docker-compose up -d
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error al iniciar los servicios"
    }
    
    Write-Host "   Servicios iniciados" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "ERROR: No se pudieron iniciar los servicios" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""

# =====================================================
# PASO 4: Esperar a que los servicios estén listos
# =====================================================
Write-Host "4. Esperando a que los servicios estén listos..." -ForegroundColor Yellow

# Esperar a que PostgreSQL esté listo
Write-Host "   Esperando PostgreSQL..." -NoNewline
$postgresReady = $false
$maxAttempts = 30
$attempt = 0

while (-not $postgresReady -and $attempt -lt $maxAttempts) {
    $attempt++
    try {
        $health = docker inspect --format='{{.State.Health.Status}}' tpi-postgres 2>&1
        if ($health -eq "healthy") {
            $postgresReady = $true
            Write-Host " OK" -ForegroundColor Green
        } else {
            Start-Sleep -Seconds 2
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $postgresReady) {
    Write-Host " Warning: PostgreSQL puede no estar completamente listo" -ForegroundColor Yellow
}

# Esperar a que Keycloak esté listo
Write-Host "   Esperando Keycloak..." -NoNewline
$keycloakReady = $false
$maxAttempts = 60
$attempt = 0

while (-not $keycloakReady -and $attempt -lt $maxAttempts) {
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9090/realms/master" -Method GET -TimeoutSec 2 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $keycloakReady = $true
            Write-Host " OK" -ForegroundColor Green
        }
    } catch {
        if ($attempt -lt $maxAttempts) {
            Start-Sleep -Seconds 3
        }
    }
}

if (-not $keycloakReady) {
    Write-Host " Warning: Keycloak puede no estar completamente listo" -ForegroundColor Yellow
    Write-Host "   Continuando de todas formas..." -ForegroundColor Yellow
}

Write-Host ""

# Esperar un poco más para que todos los servicios estén completamente listos
Write-Host "   Esperando que todos los servicios terminen de inicializar..." -ForegroundColor Gray
Write-Host "   (Keycloak puede tardar hasta 2 minutos en inicializar completamente)..." -ForegroundColor Gray
Start-Sleep -Seconds 30

# Verificar nuevamente Keycloak antes de continuar
Write-Host "   Verificando Keycloak nuevamente..." -NoNewline
$keycloakReady = $false
$maxAttempts = 40
$attempt = 0

while (-not $keycloakReady -and $attempt -lt $maxAttempts) {
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9090/realms/master" -Method GET -TimeoutSec 3 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $keycloakReady = $true
            Write-Host " OK" -ForegroundColor Green
        }
    } catch {
        if ($attempt -lt $maxAttempts) {
            Start-Sleep -Seconds 3
        }
    }
}

if (-not $keycloakReady) {
    Write-Host " Warning: Keycloak aún no está listo, pero continuaremos..." -ForegroundColor Yellow
    Write-Host "   Puedes ejecutar manualmente: .\iniciar-sistema.ps1" -ForegroundColor Yellow
}

Write-Host ""

# =====================================================
# PASO 5: Configurar Keycloak completamente
# =====================================================
Write-Host "5. Configurando Keycloak..." -ForegroundColor Yellow
Write-Host ""

# Variables de configuración de Keycloak
$KEYCLOAK_URL = "http://localhost:9090"
$GATEWAY_URL = "http://localhost:8080"
$ADMIN_USER = "admin"
$ADMIN_PASSWORD = "admin123"
$REALM = "tpi-backend"
$CLIENT_ID = "tpi-client"

# 5.1. Obtener token de admin
Write-Host "5.1. Obteniendo token de administrador..." -ForegroundColor Yellow

try {
    $tokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" `
        -Method Post `
        -ContentType "application/x-www-form-urlencoded" `
        -Body "grant_type=password&client_id=admin-cli&username=$ADMIN_USER&password=$ADMIN_PASSWORD" `
        -ErrorAction Stop
    
    $ADMIN_TOKEN = $tokenResponse.access_token
    Write-Host "   Token obtenido" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: No se pudo obtener el token de administrador" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 5.2. Verificar/Crear Realm
Write-Host "5.2. Verificando/Creando realm '$REALM'..." -ForegroundColor Yellow

$realmExists = $false
try {
    $realmCheck = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM" `
        -Method Get `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
        -ErrorAction Stop
    $realmExists = $true
    Write-Host "   Realm '$REALM' ya existe" -ForegroundColor Green
} catch {
    $realmExists = $false
}

if (-not $realmExists) {
    $realmBody = @{
        realm = $REALM
        enabled = $true
        displayName = "TPI Backend"
        accessTokenLifespan = 1800
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
            -Body $realmBody `
            -ErrorAction Stop
        
        Write-Host "   Realm '$REALM' creado" -ForegroundColor Green
        
        Write-Host "   Esperando que Keycloak propague el realm..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
        
        $realmAvailable = $false
        $verificationAttempts = 0
        $maxVerificationAttempts = 10
        
        while (-not $realmAvailable -and $verificationAttempts -lt $maxVerificationAttempts) {
            $verificationAttempts++
            try {
                $realmVerify = Invoke-WebRequest -Uri "$KEYCLOAK_URL/realms/$REALM" `
                    -Method Get `
                    -TimeoutSec 3 `
                    -ErrorAction Stop
                if ($realmVerify.StatusCode -eq 200) {
                    $realmAvailable = $true
                    Write-Host "   Realm '$REALM' verificado y disponible" -ForegroundColor Green
                }
            } catch {
                if ($verificationAttempts -lt $maxVerificationAttempts) {
                    Start-Sleep -Seconds 2
                }
            }
        }
    } catch {
        Write-Host "   ERROR al crear el realm: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# 5.3. Configurar duración del token
Write-Host "5.3. Configurando duración del token a 30 minutos..." -ForegroundColor Yellow

try {
    $realmConfig = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM" `
        -Method Get `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
        -ErrorAction Stop
    
    $realmConfig.accessTokenLifespan = 1800
    $tokenSettings = $realmConfig | ConvertTo-Json -Depth 10
    
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM" `
        -Method Put `
        -ContentType "application/json" `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
        -Body $tokenSettings `
        -ErrorAction Stop
    
    Write-Host "   Token configurado para 30 minutos (1800 segundos)" -ForegroundColor Green
} catch {
    Write-Host "   Error configurando duración del token: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 5.4. Crear Roles
Write-Host "5.4. Creando roles..." -ForegroundColor Yellow

$roles = @("CLIENTE", "OPERADOR", "TRANSPORTISTA")

foreach ($role in $roles) {
    $roleBody = @{
        name = $role
        description = "Rol $role"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/roles" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
            -Body $roleBody `
            -ErrorAction Stop
        
        Write-Host "   Rol '$role' creado" -ForegroundColor Green
    } catch {
        Write-Host "   Rol '$role' ya existe" -ForegroundColor Yellow
    }
}

Write-Host ""

# 5.5. Configurar Cliente
Write-Host "5.5. Configurando cliente '$CLIENT_ID'..." -ForegroundColor Yellow

try {
    $clients = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/clients?clientId=$CLIENT_ID" `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
        -ErrorAction Stop

    if ($clients.Count -gt 0) {
        Write-Host "   Cliente existe, eliminando..." -ForegroundColor Yellow
        $clientInternalId = $clients[0].id
        
        try {
            Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/clients/$clientInternalId" `
                -Method Delete `
                -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
                -ErrorAction Stop
            
            Write-Host "   Cliente eliminado" -ForegroundColor Green
            Start-Sleep -Seconds 2
        } catch {
            Write-Host "   Warning: No se pudo eliminar el cliente existente" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "   No se encontró cliente existente" -ForegroundColor Gray
}

$clientBody = @{
    clientId = $CLIENT_ID
    name = "TPI Backend Client"
    publicClient = $true
    directAccessGrantsEnabled = $true
    standardFlowEnabled = $true
    enabled = $true
    redirectUris = @("*")
    webOrigins = @("*")
    rootUrl = "http://localhost:8080"
    protocol = "openid-connect"
    fullScopeAllowed = $true
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/clients" `
        -Method Post `
        -ContentType "application/json" `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
        -Body $clientBody `
        -ErrorAction Stop

    Write-Host "   Cliente '$CLIENT_ID' creado como PUBLICO" -ForegroundColor Green
    Start-Sleep -Seconds 2
} catch {
    Write-Host "   ERROR al crear el cliente: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 5.6. Crear Usuarios y Asignar Roles
Write-Host "5.6. Creando usuarios y asignando roles..." -ForegroundColor Yellow

$usuarios = @(
    @{username = "cliente@tpi.com"; password = "cliente123"; email = "cliente@tpi.com"; rol = "CLIENTE"; nombre = "Cliente"; apellido = "TPI"},
    @{username = "operador@tpi.com"; password = "operador123"; email = "operador@tpi.com"; rol = "OPERADOR"; nombre = "Operador"; apellido = "TPI"},
    @{username = "transportista@tpi.com"; password = "transportista123"; email = "transportista@tpi.com"; rol = "TRANSPORTISTA"; nombre = "Transportista"; apellido = "TPI"}
)

foreach ($usuario in $usuarios) {
    Write-Host "   Procesando $($usuario.username)..." -ForegroundColor Cyan
    
    # Crear usuario
    $userBody = @{
        username = $usuario.username
        email = $usuario.email
        emailVerified = $true
        enabled = $true
        firstName = $usuario.nombre
        lastName = $usuario.apellido
        credentials = @(
            @{
                type = "password"
                value = $usuario.password
                temporary = $false
            }
        )
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/users" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
            -Body $userBody `
            -ErrorAction Stop
        
        Write-Host "      Usuario creado" -ForegroundColor Green
    } catch {
        Write-Host "      Usuario ya existe" -ForegroundColor Yellow
    }
    
    # Obtener ID del usuario
    $users = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/users?username=$($usuario.username)" `
        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"}
    
    if ($users.Count -gt 0) {
        $userId = $users[0].id
        
        # Obtener el rol
        $roleMapping = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/roles/$($usuario.rol)" `
            -Headers @{Authorization = "Bearer $ADMIN_TOKEN"}
        
        if ($roleMapping) {
            # Verificar si ya tiene el rol
            $existingRoles = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/users/$userId/role-mappings/realm" `
                -Headers @{Authorization = "Bearer $ADMIN_TOKEN"}
            
            $hasRole = $false
            foreach ($existingRole in $existingRoles) {
                if ($existingRole.name -eq $usuario.rol) {
                    $hasRole = $true
                    break
                }
            }
            
            if (-not $hasRole) {
                # Asignar rol
                $rolesToAssign = @(
                    @{
                        id = $roleMapping.id
                        name = $roleMapping.name
                    }
                )
                
                # Convertir a JSON asegurando que sea un array
                $rolesJson = $rolesToAssign | ConvertTo-Json
                if (-not $rolesJson.StartsWith('[')) {
                    $rolesJson = "[$rolesJson]"
                }
                
                try {
                    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM/users/$userId/role-mappings/realm" `
                        -Method Post `
                        -ContentType "application/json" `
                        -Headers @{Authorization = "Bearer $ADMIN_TOKEN"} `
                        -Body $rolesJson `
                        -ErrorAction Stop
                    
                    Write-Host "      Rol '$($usuario.rol)' asignado" -ForegroundColor Green
                } catch {
                    Write-Host "      Error al asignar rol: $($_.Exception.Message)" -ForegroundColor Red
                }
            } else {
                Write-Host "      Rol '$($usuario.rol)' ya estaba asignado" -ForegroundColor Cyan
            }
        }
    }
}

Write-Host ""

# 5.7. Verificar configuración final
Write-Host "5.7. Verificando configuración final del realm..." -ForegroundColor Yellow

$realmReady = $false
$readyAttempts = 0
$maxReadyAttempts = 15

while (-not $realmReady -and $readyAttempts -lt $maxReadyAttempts) {
    $readyAttempts++
    try {
        $realmPublic = Invoke-WebRequest -Uri "$KEYCLOAK_URL/realms/$REALM/.well-known/openid-configuration" `
            -Method Get `
            -TimeoutSec 3 `
            -ErrorAction Stop
        
        if ($realmPublic.StatusCode -eq 200) {
            $realmReady = $true
            Write-Host "   Realm '$REALM' completamente configurado y disponible" -ForegroundColor Green
        }
    } catch {
        if ($readyAttempts -lt $maxReadyAttempts) {
            Start-Sleep -Seconds 2
        }
    }
}

if (-not $realmReady) {
    Write-Host "   Warning: El realm puede no estar completamente listo" -ForegroundColor Yellow
}

Write-Host ""

# =====================================================
# PASO 6: Obtener Tokens para Testing
# =====================================================
Write-Host "6. Obteniendo tokens para los 3 roles..." -ForegroundColor Yellow
Write-Host ""

$usuariosTokens = @(
    @{ Rol = "CLIENTE"; Username = "cliente@tpi.com"; Password = "cliente123"; VarPrefix = "CLIENTE" },
    @{ Rol = "OPERADOR"; Username = "operador@tpi.com"; Password = "operador123"; VarPrefix = "OPERADOR" },
    @{ Rol = "TRANSPORTISTA"; Username = "transportista@tpi.com"; Password = "transportista123"; VarPrefix = "TRANSPORTISTA" }
)

$exitosos = 0

foreach ($usuario in $usuariosTokens) {
    Write-Host "   $($usuario.Rol)..." -ForegroundColor Yellow
    $body = @{ username = $usuario.Username; password = $usuario.Password } | ConvertTo-Json
    try {
        $r = Invoke-RestMethod -Uri "$GATEWAY_URL/auth/login" -Method Post -ContentType "application/json" -Body $body -ErrorAction Stop
        Set-Item -Path "Env:$($usuario.VarPrefix)_TOKEN" -Value $r.access_token
        Set-Item -Path "Env:$($usuario.VarPrefix)_REFRESH" -Value $r.refresh_token
        Write-Host "      OK - Expira en: $($r.expires_in)s" -ForegroundColor Green
        $exitosos++
    } catch {
        Write-Host "      Error al obtener token" -ForegroundColor Red
    }
}

Write-Host ""
if ($exitosos -eq 3) {
    Write-Host "   $exitosos/3 tokens configurados correctamente" -ForegroundColor Green
} else {
    Write-Host "   $exitosos/3 tokens configurados (algunos pueden haber fallado)" -ForegroundColor Yellow
}

Write-Host ""

# =====================================================
# RESUMEN FINAL
# =====================================================
Write-Host "========================================" -ForegroundColor Green
Write-Host "SISTEMA LISTO PARA USAR CON POSTMAN" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Servicios disponibles:" -ForegroundColor Cyan
Write-Host "  * PostgreSQL: localhost:5432" -ForegroundColor White
Write-Host "  * Keycloak Admin: http://localhost:9090/admin" -ForegroundColor White
Write-Host "  * API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  * Servicio Gestión: http://localhost:8081/api/gestion" -ForegroundColor White
Write-Host "  * Servicio Flota: http://localhost:8082/api/flota" -ForegroundColor White
Write-Host "  * Servicio Logística: http://localhost:8083/api/logistica" -ForegroundColor White
Write-Host ""

Write-Host "Usuarios de prueba:" -ForegroundColor Cyan
Write-Host "  * cliente@tpi.com / cliente123 (CLIENTE)" -ForegroundColor White
Write-Host "  * operador@tpi.com / operador123 (OPERADOR)" -ForegroundColor White
Write-Host "  * transportista@tpi.com / transportista123 (TRANSPORTISTA)" -ForegroundColor White
Write-Host ""

Write-Host "Comandos útiles:" -ForegroundColor Cyan
Write-Host "  * Ver logs: docker-compose logs -f" -ForegroundColor Gray
Write-Host "  * Detener: docker-compose down" -ForegroundColor Gray
Write-Host "  * Reiniciar: docker-compose restart" -ForegroundColor Gray
Write-Host ""

Write-Host "¡Sistema listo para usar con Postman!" -ForegroundColor Green
Write-Host ""

