"""Errors shared by the local-only StreamForge fixture tools."""


class DatasetContractError(ValueError):
    """Raised when a fixture or record violates the published data contract."""
