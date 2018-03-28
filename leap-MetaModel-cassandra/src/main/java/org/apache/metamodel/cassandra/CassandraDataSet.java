/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.metamodel.cassandra;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.metamodel.data.AbstractDataSet;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;

import com.google.common.reflect.TypeToken;

/**
 * A {@link DataSet} implementation that wraps a iterator of
 * {@link com.datastax.driver.core.Row}.
 */
final class CassandraDataSet extends AbstractDataSet {

	private final Iterator<com.datastax.driver.core.Row> _cursor;
	private final Map<String, TypeToken<?>> _typeTokenConverters;
	private volatile com.datastax.driver.core.Row _dbObject;

	public CassandraDataSet(Iterator<com.datastax.driver.core.Row> cursor, List<Column> columns,
			Map<String, TypeToken<?>> typeTokenConverters) {
		super(columns.stream().map(SelectItem::new).collect(Collectors.toList()));
		_cursor = cursor;
		_typeTokenConverters = typeTokenConverters;
	}

	@Override
	public boolean next() {
		if (_cursor.hasNext()) {
			_dbObject = _cursor.next();
			return true;
		} else {
			_dbObject = null;
			return false;
		}
	}

	@Override
	public Row getRow() {
		Row row =  CassandraUtils.toRow(_dbObject, getHeader(),_typeTokenConverters);
 		return row;

	}

}
