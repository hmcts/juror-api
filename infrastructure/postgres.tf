data "azurerm_subnet" "postgres" {
  name                 = "iaas"
  resource_group_name  = "ss-${var.env}-network-rg"
  virtual_network_name = "ss-${var.env}-vnet"
}

module "postgresql_flexible" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source              = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env                 = var.env
  product             = var.product
  resource_group_name = local.rg_name
  component           = var.component
  business_area       = "sds"
  location            = var.location
  pgsql_sku           = "GP_Standard_D4s_v3"
  pgsql_storage_mb    = var.pgsql_storage_mb
  pgsql_storage_tier  = var.pgsql_storage_tier

  common_tags          = var.common_tags
  admin_user_object_id = var.jenkins_AAD_objectId
  pgsql_databases = [
    {
      name : local.db_name
    }
  ]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "dblink,tablefunc"
    },
    {
      name  = "backslash_quote"
      value = "on"
    },
    {
      name  = "azure.enable_temp_tablespaces_on_local_ssd"
      value = "off"
    }
  ]
  pgsql_version = "16"
}

data "azurerm_key_vault" "key_vault" {
  name                = local.vault_name
  resource_group_name = local.rg_name
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-CONNECTION-STRING" {
  name         = "${var.component}-POSTGRES-CONNECTION-STRING"
  value        = "jdbc:postgresql://${module.postgresql_flexible.fqdn}:${local.db_port}/${local.db_name}"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.postgresql_flexible.username
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.postgresql_flexible.password
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.postgresql_flexible.fqdn
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = local.db_port
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = local.db_name
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_client_config" "current" {}

data "azuread_group" "dts_jit_access_juror_db_admin" {
  display_name = "DTS JIT Access Juror DB Admin"
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "jit" {
  server_name         = "juror-api-${var.env}"
  resource_group_name = local.rg_name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = data.azuread_group.dts_jit_access_juror_db_admin.object_id
  principal_name      = data.azuread_group.dts_jit_access_juror_db_admin.display_name
  principal_type      = "Group"

  depends_on = [module.postgresql_flexible]
}

