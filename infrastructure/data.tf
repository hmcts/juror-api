data "azurerm_subnet" "postgres" {
  name                 = "iaas"
  resource_group_name  = "ss-${var.env}-network-rg"
  virtual_network_name = "ss-${var.env}-vnet"
}

data "azurerm_key_vault" "key_vault" {
  name                = local.vault_name
  resource_group_name = local.rg_name
}

data "azurerm_client_config" "current" {}

data "azuread_group" "dts_jit_access_juror_db_admin" {
  display_name = "DTS JIT Access Juror DB Admin"
}
