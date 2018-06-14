<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java">
	<xsl:template match="/">
		<xsl:variable name="field">
			<xsl:value-of select="//EVENTPAYLOADNAME" />
		</xsl:variable>
		<xsl:element name="LIST_CANCEL">
			<xsl:element name="CTRL_SEG">
				<xsl:element name="TRANID">
					<xsl:value-of select="//EVENTSEQ" />
				</xsl:element>
				<xsl:element name="TRANDT">
					<xsl:value-of select="java:util.Date.new()" />
				</xsl:element>
				<xsl:element name="WCS_ID">
					<xsl:value-of select="//WCSID" />
				</xsl:element>
				<xsl:element name="WH_ID">
					<xsl:value-of select="//WHID" />
				</xsl:element>
				<xsl:element name="LIST_CAN_SEG">
					<xsl:for-each select="//LISTINFORMATION">
						<xsl:element name="LISTID">
							<xsl:value-of select="LISTID" />
						</xsl:element>
					</xsl:for-each>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>