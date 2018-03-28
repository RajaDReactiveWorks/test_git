package org.apache.ddlutils.model;

public class PartitionSchema {
	private String name;
	private PartitionFunction partitionFunction;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PartitionFunction getPartitionFunction() {
		return partitionFunction;
	}

	public void setPartitionFunction(PartitionFunction partitionFunction) {
		this.partitionFunction = partitionFunction;
	}

	@Override
	public String toString() {
		return "PartitionSchema [name=" + name + ", partitionFunction=" + partitionFunction + "]";
	}

}
