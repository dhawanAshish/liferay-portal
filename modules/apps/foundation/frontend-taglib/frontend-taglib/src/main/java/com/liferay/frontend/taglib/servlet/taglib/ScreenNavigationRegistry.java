/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.frontend.taglib.servlet.taglib;

import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceComparator;
import com.liferay.osgi.service.tracker.collections.map.ServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.StringPool;

import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = ScreenNavigationRegistry.class)
public class ScreenNavigationRegistry {

	public List<ScreenNavigationCategory> getScreenNavigationCategories(
		String screenNavigationId) {

		return _screenNavigationCategoriesMap.getService(screenNavigationId);
	}

	public List<ScreenNavigationEntry> getScreenNavigationEntries(
		ScreenNavigationCategory screenNavigationCategory) {

		String key = _getKey(
			screenNavigationCategory.getScreenNavigationKey(),
			screenNavigationCategory.getCategoryKey());

		return _screenNavigationEntriesMap.getService(key);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_screenNavigationEntriesMap =
			ServiceTrackerMapFactory.openMultiValueMap(
				bundleContext, ScreenNavigationEntry.class, null,
				new ScreenNavigationEntriesServiceReferenceMapper(),
				Collections.reverseOrder(
					new PropertyServiceReferenceComparator(
						"screen.navigation.entry.order")));

		_screenNavigationCategoriesMap =
			ServiceTrackerMapFactory.openMultiValueMap(
				bundleContext, ScreenNavigationCategory.class, null,
				new ScreenNavigationCategoriesServiceReferenceMapper(),
				Collections.reverseOrder(
					new PropertyServiceReferenceComparator(
						"screen.navigation.category.order")));
	}

	@Deactivate
	protected void deactivate() {
		_screenNavigationCategoriesMap.close();
		_screenNavigationEntriesMap.close();
	}

	private String _getKey(
		String screenNavigationId, String screenCategoryKey) {

		return screenNavigationId + StringPool.PERIOD + screenCategoryKey;
	}

	private BundleContext _bundleContext;
	private ServiceTrackerMap<String, List<ScreenNavigationCategory>>
		_screenNavigationCategoriesMap;
	private ServiceTrackerMap<String, List<ScreenNavigationEntry>>
		_screenNavigationEntriesMap;

	private class ScreenNavigationCategoriesServiceReferenceMapper
		implements ServiceReferenceMapper<String, ScreenNavigationCategory> {

		@Override
		public void map(
			ServiceReference<ScreenNavigationCategory> serviceReference,
			Emitter<String> emitter) {

			ScreenNavigationCategory screenNavigationCategory =
				_bundleContext.getService(serviceReference);

			try {
				emitter.emit(screenNavigationCategory.getScreenNavigationKey());
			}
			finally {
				_bundleContext.ungetService(serviceReference);
			}
		}

	}

	private class ScreenNavigationEntriesServiceReferenceMapper
		implements ServiceReferenceMapper<String, ScreenNavigationEntry> {

		@Override
		public void map(
			ServiceReference<ScreenNavigationEntry> serviceReference,
			Emitter<String> emitter) {

			ScreenNavigationEntry screenNavigationEntry =
				_bundleContext.getService(serviceReference);

			try {
				String key = _getKey(
					screenNavigationEntry.getScreenNavigationKey(),
					screenNavigationEntry.getCategoryKey());

				emitter.emit(key);
			}
			finally {
				_bundleContext.ungetService(serviceReference);
			}
		}

	}

}