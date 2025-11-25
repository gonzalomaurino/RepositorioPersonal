# =====================================================
# DOCKERFILE - KEYCLOAK PERSONALIZADO
# =====================================================
# Extiende la imagen oficial de Keycloak e instala curl
# para permitir healthchecks confiables
# =====================================================

FROM quay.io/keycloak/keycloak:26.0.7

USER root

# Instalar curl para healthchecks
# Keycloak usa imagen UBI (Red Hat), que puede tener microdnf o dnf
# Intentamos ambos métodos para compatibilidad
RUN (microdnf install -y curl || dnf install -y curl || yum install -y curl) && \
    (microdnf clean all || dnf clean all || yum clean all) || true

# Volver al usuario keycloak (si existe, si no quedamos en root)
USER 1000

# El comando se pasa desde docker-compose.yml

