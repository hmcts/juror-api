provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "sdp_vault"
  subscription_id            = var.aks_subscription_id
}

module "sdp_db_user" {

  providers = {
    azurerm.sdp_vault = azurerm.sdp_vault
  }

  source = "git@github.com:hmcts/terraform-module-sdp-db-user?ref=master"

  env = var.env
  server_name       = "${var.product}-${var.component}-${var.env}"
  server_fqdn       = module.postgresql_flexible.fqdn
  server_admin_user = module.postgresql_flexible.username
  server_admin_pass = module.postgresql_flexible.password

  count = var.env == "sandbox" || var.env == "dev" || var.env == "test" || var.env == "demo" ? 0 : 1

  databases = [
    {
      name : "juror"
    }
  ]

  database_schemas = {
    juror = ["juror_mod"]
  }

  common_tags = var.common_tags

  depends_on = [
    module.postgresql_flexible
  ]
}