package org.smssecure.smssecure.components.emoji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.components.KeyboardAwareLinearLayout;
import org.smssecure.smssecure.components.RepeatableImageKey;
import org.smssecure.smssecure.components.RepeatableImageKey.KeyEventListener;
import org.smssecure.smssecure.components.emoji.EmojiPageFragment.EmojiSelectionListener;
import org.smssecure.smssecure.util.ResUtil;

import java.util.LinkedList;
import java.util.List;

public class EmojiDrawer extends Fragment {
  private static final KeyEvent DELETE_KEY_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);

  private EmojiEditText             composeText;
  private KeyboardAwareLinearLayout container;
  private ViewPager                 pager;
  private PagerSlidingTabStrip      strip;

  public static EmojiDrawer newInstance(@ArrayRes int categories, @ArrayRes int icons) {
    final EmojiDrawer fragment = new EmojiDrawer();
    final Bundle      args     = new Bundle();
    args.putInt("categories", categories);
    args.putInt("icons", icons);
    fragment.setArguments(args);
    return fragment;
  }

  public static EmojiDrawer newInstance() {
    return newInstance(R.array.emoji_categories, R.array.emoji_category_icons);
  }

  public void setComposeEditText(EmojiEditText composeText) {
    this.composeText = composeText;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.emoji_drawer, container, false);
    initializeResources(v);
    initializeEmojiGrid();
    return v;
  }

  private void initializeResources(View v) {
    Log.w("EmojiDrawer", "initializeResources()");
    this.container = (KeyboardAwareLinearLayout) v.findViewById(R.id.container);
    this.pager     = (ViewPager)                 v.findViewById(R.id.emoji_pager);
    this.strip     = (PagerSlidingTabStrip)      v.findViewById(R.id.tabs);

    RepeatableImageKey backspace = (RepeatableImageKey)v.findViewById(R.id.backspace);
    backspace.setOnKeyEventListener(new KeyEventListener() {
      @Override public void onKeyEvent() {
        if (composeText != null && composeText.getText().length() > 0) {
          composeText.dispatchKeyEvent(DELETE_KEY_EVENT);
        }
      }
    });
  }

  public void hide() {
    container.setVisibility(View.GONE);
  }

  public void show() {
    int keyboardHeight = container.getKeyboardHeight();
    Log.w("EmojiDrawer", "setting emoji drawer to height " + keyboardHeight);
    container.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, keyboardHeight));
    container.requestLayout();
    container.setVisibility(View.VISIBLE);
  }

  public boolean isOpen() {
    return container.getVisibility() == View.VISIBLE;
  }

  private void initializeEmojiGrid() {

    pager.setAdapter(new EmojiPagerAdapter(getActivity(),
                                           getFragmentManager(),
                                           getPageModels(getArguments().getInt("categories"),
                                                         getArguments().getInt("icons")),
                                           new EmojiSelectionListener() {
                                             @Override public void onEmojiSelected(int emojiCode) {
                                               composeText.insertEmoji(emojiCode);
                                             }
                                           }));
    strip.setViewPager(pager);
  }

  private List<EmojiPageModel> getPageModels(@ArrayRes int pagesRes, @ArrayRes int iconsRes) {
    final int[] icons = ResUtil.getResourceIds(getActivity(), iconsRes);
    final int[] pages = ResUtil.getResourceIds(getActivity(), pagesRes);
    final List<EmojiPageModel> models = new LinkedList<>();
    models.add(new RecentEmojiPageModel(getActivity()));
    for (int i = 0; i < icons.length; i++) {
      models.add(new StaticEmojiPageModel(icons[i], getResources().getIntArray(pages[i])));
    }
    return models;
  }

  public static class EmojiPagerAdapter extends FragmentStatePagerAdapter
      implements PagerSlidingTabStrip.CustomTabProvider
  {
    private Context                context;
    private List<EmojiPageModel>   pages;
    private EmojiSelectionListener listener;

    public EmojiPagerAdapter(@NonNull Context context,
                             @NonNull FragmentManager fm,
                             @NonNull List<EmojiPageModel> pages,
                             @Nullable EmojiSelectionListener listener)
    {
      super(fm);
      this.context  = context;
      this.pages    = pages;
      this.listener = listener;
    }

    @Override
    public int getCount() {
      return pages.size();
    }

    @Override public Fragment getItem(int i) {
      return EmojiPageFragment.newInstance(pages.get(i), listener);
    }

    @Override public View getCustomTabView(ViewGroup viewGroup, int i) {
      ImageView image = new ImageView(context);
      image.setImageResource(pages.get(i).getIconRes());
      return image;
    }
  }
}
