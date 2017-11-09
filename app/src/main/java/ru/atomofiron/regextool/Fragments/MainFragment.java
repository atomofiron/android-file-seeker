package ru.atomofiron.regextool.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Adapters.FilesAdapter;
import ru.atomofiron.regextool.Adapters.HistoryAdapter;
import ru.atomofiron.regextool.Adapters.ListAdapter;
import ru.atomofiron.regextool.Adapters.ViewPagerAdapter;
import ru.atomofiron.regextool.CustomViews.RegexText;
import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.MainActivity;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.SearchService;
import ru.atomofiron.regextool.Utils.Permissions;


public class MainFragment extends Fragment {
	private static final String KEY_QUERY = "KEY_QUERY";
	private static final String KEY_TEST = "KEY_TEST";
	private static final String KEY_SELECTED = "KEY_SELECTED";
	private static final String KEY_FLAG_CASE = "KEY_FLAG_CASE";
	private static final String KEY_FLAG_IN_FILES = "KEY_FLAG_IN_FILES";
	private static final String KEY_FLAG_REGEXP = "KEY_FLAG_REGEXP";
	private static final String KEY_FLAG_MULTILINE = "KEY_FLAG_MULTILINE";

	private Activity ac;
	private View fragmentView;

	private RegexText regexText;
	private CheckBox caseToggle;
	private CheckBox infilesToggle;
	private CheckBox regexToggle;
	private CheckBox multilineToggle;
	private EditText testField;
	private ListView filesListView;

	private ListAdapter selectedListAdapter;
	private LocalBroadcastManager broadcastManager;
	private Receiver resultReceiver;
	private SharedPreferences sp;
	private ListView historyList;
	private String defPath;

	private OnResultListener onResultListener = null;

	private MainActivity mainActivity;

	public MainFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ac = getActivity();
		mainActivity = (MainActivity) ac;
		sp = I.sp(ac);

		resultReceiver = new Receiver(ac);
		broadcastManager = LocalBroadcastManager.getInstance(ac);
		broadcastManager.registerReceiver(resultReceiver, new IntentFilter(I.toMainActivity));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if ((fragmentView = getView()) != null) {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null)
				parent.removeView(fragmentView);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (fragmentView != null)
			return fragmentView;

		defPath = sp.getString(I.PREF_STORAGE_PATH, "/");
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		regexText = (RegexText) view.findViewById(R.id.regex_text);
		caseToggle = (CheckBox) view.findViewById(R.id.case_sense);
		infilesToggle = (CheckBox) view.findViewById(R.id.in_files);
		regexToggle = (CheckBox) view.findViewById(R.id.simple_search);
		multilineToggle = (CheckBox) view.findViewById(R.id.multiline);
		regexToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				regexText.checkPatternValid(isChecked);
				multilineToggle.setEnabled(isChecked);
			}
		});

		ButtonListener listener = new ButtonListener();
		view.findViewById(R.id.go).setOnClickListener(listener);
		view.findViewById(R.id.slash).setOnClickListener(listener);
		view.findViewById(R.id.box).setOnClickListener(listener);
		view.findViewById(R.id.nobox).setOnClickListener(listener);
		view.findViewById(R.id.dot).setOnClickListener(listener);
		view.findViewById(R.id.star).setOnClickListener(listener);
		view.findViewById(R.id.dash).setOnClickListener(listener);
		view.findViewById(R.id.roof).setOnClickListener(listener);
		view.findViewById(R.id.buck).setOnClickListener(listener);

		ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
		ArrayList<View> viewList = new ArrayList<>();

		ListView selectedListView = new ListView(ac);
		selectedListAdapter = new ListAdapter(ac);
		selectedListAdapter.update();
		selectedListView.setAdapter(selectedListAdapter);
		selectedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				selectedListAdapter.remove(position);
				return true;
			}
		});
		selectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedListAdapter.onItemClick(view);
			}
		});

		filesListView = new ListView(ac);
		final FilesAdapter filesListAdapter = new FilesAdapter(ac, filesListView);
		filesListView.setAdapter(filesListAdapter);
		filesListAdapter.update(new File(defPath));

		testField = (EditText) LayoutInflater.from(ac).inflate(R.layout.edittext_test, null);
		regexText.setTestField(testField);

		viewList.add(testField);
		viewList.add(selectedListView);
		viewList.add(filesListView);
		ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(ac, viewList);
		viewPager.setAdapter(pagerAdapter);

		((TabLayout) view.findViewById(R.id.tab_layout)).setupWithViewPager(viewPager);

		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			public void onPageSelected(int position) {
				if (position == 1)
					selectedListAdapter.update();
				else if (position == 2)
					filesListAdapter.updateSelected();
			}
			public void onPageScrollStateChanged(int state) {}
		});

		historyList = (ListView) mainActivity.findViewById(R.id.history_list);
		historyList.setAdapter(new HistoryAdapter(historyList, new HistoryAdapter.OnItemClickListener() {
			public void onItemClick(String node) {
				regexText.setText(node);
				regexText.setSelection(node.length());
				((DrawerLayout) mainActivity.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
			}
		}));

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(KEY_QUERY, regexText.getText().toString());
		outState.putString(KEY_TEST, testField.getText().toString());
		outState.putStringArrayList(KEY_SELECTED, selectedListAdapter.getCheckedPathArray());
		outState.putBoolean(KEY_FLAG_CASE, caseToggle.isChecked());
		outState.putBoolean(KEY_FLAG_IN_FILES, infilesToggle.isChecked());
		outState.putBoolean(KEY_FLAG_REGEXP, regexToggle.isChecked());
		outState.putBoolean(KEY_FLAG_MULTILINE, multilineToggle.isChecked());
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState == null)
			return;

		regexText.setText(savedInstanceState.getString(KEY_QUERY, ""));
		testField.setText(savedInstanceState.getString(KEY_TEST, ""));
		selectedListAdapter.setCheckedPathsList(savedInstanceState.getStringArrayList(KEY_SELECTED));
		caseToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_CASE));
		infilesToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_IN_FILES));
		regexToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_REGEXP));
		multilineToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_MULTILINE));
	}

	@Override
	public void onStart() {
		super.onStart();
		String path = sp.getString(I.PREF_STORAGE_PATH, "/");
		if (!defPath.equals(path)) // в onCreateView() это не прокатывает
			((FilesAdapter) filesListView.getAdapter()).update(new File((defPath = path)));
		else
			((FilesAdapter) filesListView.getAdapter()).update();
	}

	public void checkListForSearch() {
		if (selectedListAdapter.getCheckedCount() == 0) {
			snack(R.string.no_checked);
		} else
			search();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			if (requestCode == I.REQUEST_FOR_SEARCH)
				checkListForSearch();
	}

	public void search() {
		resultReceiver.alertDialog.show();

		ac.startService(new Intent(ac, SearchService.class)
				.putExtra(I.CASE_SENSE, caseToggle.isChecked())
				.putExtra(I.SEARCH_LIST, selectedListAdapter.getCheckedPathArray())
				.putExtra(I.QUERY, regexText.getText().toString())
				.putExtra(I.SEARCH_IN_FILES, infilesToggle.isChecked())
				.putExtra(I.SEARCH_REGEX, regexToggle.isChecked())
				.putExtra(I.MULTILINE, multilineToggle.isChecked()));
	}

	public void setOnResultListener(OnResultListener listener) {
		onResultListener = listener;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		broadcastManager.unregisterReceiver(resultReceiver);
		ac.stopService(new Intent(ac, SearchService.class));
	}

	private void snack(int id) {
		snack(getString(id));
	}

	private void snack(String str) {
		I.snack(getView(), str, false);
	}




// -------------------------------------------------------------

	public interface OnResultListener {
		public void onResult(Bundle bundle);
	}

	private class ButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String symbol;
			switch (v.getId()) {
				case R.id.go:
					resultReceiver.needShowResults = true;
					String regex = regexText.getText().toString();
					if (regex.isEmpty())
						return;

					((HistoryAdapter)historyList.getAdapter()).addItem(regex);

					if (!regexToggle.isChecked())
						try { Pattern.compile(regex);
						} catch (Exception ignored) {
							snack(R.string.bad_ex);
							return;
						}
					if (regex.length() > 0 && Permissions.checkPerm(mainActivity, I.REQUEST_FOR_SEARCH))
						checkListForSearch();
					return;
				default:
					symbol = ((Button)v).getText().toString();
					break;
			}
			int start = regexText.getSelectionStart();
			regexText.getText().insert(start, symbol);
			regexText.setSelection(start+1, start+1);
		}
	}

	class Receiver extends BroadcastReceiver {
		private AlertDialog alertDialog;
		boolean needShowResults = true;

		Receiver(Context co) {
			alertDialog = new AlertDialog.Builder(co)
					.setView(R.layout.layout_searching)
					.setCancelable(false)
					.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							needShowResults = false;
							ac.stopService(new Intent(ac, SearchService.class));
						}
					})
					.setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ac.stopService(new Intent(ac, SearchService.class));
						}
					})
					.create();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int i = intent.getIntExtra(I.SEARCH_CODE, I.SEARCH_ERROR);
			I.log("onReceive(): "+i);
			alertDialog.cancel();
			switch (i) {
				case I.SEARCH_ERROR:
					snack(R.string.error);
					break;
				case I.SEARCH_NOTHING:
					if (needShowResults)
						snack(R.string.nothing);
					break;
				default:
					if (needShowResults) {
						snack(getString(R.string.results, i));
						if (onResultListener != null)
							onResultListener.onResult(intent.getExtras());
					}
					break;
			}
		}
	}
}
