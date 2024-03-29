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

  env               = var.env
  server_name       = "${var.product}-${var.component}-flexible-${var.env}"
  server_fqdn       = module.postgresql_flexible.fqdn
  server_admin_user = module.postgresql_flexible.username
  server_admin_pass = module.postgresql_flexible.password

  count = var.env == "prod" || var.env == "stg" || var.env == "ithc" ? 1 : 0

  databases = [
    {
      name : "juror"
    }
  ]

  database_schemas = {
    juror = ["juror_mod", "juror_dashboard"]
  }

  common_tags = var.common_tags

  depends_on = [
    module.postgresql_flexible
  ]
}
