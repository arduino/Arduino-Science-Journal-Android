# Your first PR

## Prerequisites

Before you start working on Science Journal Android, make sure you've read [CONTRIBUTING.md](https://github.com/bcmi-labs/Science-Journal-Android/blob/master/CONTRIBUTING.md).

## Finding things to work on

If you want to work on something else, e.g. new functionality or fixing a bug, it would be helpful if you submit a new issue so we have a chance to discuss it first. We might have some pointers for you on how to get started, or how to best integrate it with existing solutions.

## Checking out the Science Journal Android repo

- Click the “Fork” button in the upper right corner of the [main Science Journal Android repo](https://github.com/bcmi-labs/Science-Journal-Android)
- Clone your fork:
  - `git clone git@github.com:<YOUR_GITHUB_USER>/Science-Journal-Android.git`
  - Learn more about how to manage your fork: [https://help.github.com/articles/working-with-forks/](https://help.github.com/articles/working-with-forks/)
- Install dependencies:
- Create a new branch to work on:
  - `git checkout -b <YOUR_BRANCH_NAME>`
  - A good name for a branch describes the thing you’ll be working on, e.g. `issue-132`, `fix-pitch-sensor-graph`, etc.
That’s it! Now you’re ready to work on Arduino Science Journal Android

## Testing your changes

Test on device! We ask you test your change on Android.

## Submitting a PR

When the coding is done and you’ve finished testing your changes, you're ready to submit a PR to the [Arduino Science Journal Android main repo](https://github.com/bcmi-labs/Science-Journal-Android). Everything you need to know about submitting the PR itself is inside our [Pull Request Template](https://github.com/bcmi-labs/Science-Journal-Android/blob/master/PULL_REQUEST_TEMPLATE.md). Some best practices are:

- Use a descriptive title
- Link the issues related to your PR in the body

## After the review

Once a core member has reviewed your PR, you might need to make changes before it gets merged. To make it easier on us, please make sure to avoid using `git commit --amend` or force pushes to make corrections. By avoiding rewriting the commit history, you will allow each round of edits to become its own visible commit. This helps the people who need to review your code easily understand exactly what has changed since the last time they looked. Feel free to use whatever commit messages you like, as we will squash them anyway. When you are done addressing your review, also add a small comment like “Feedback addressed @<your_reviewer>”.

Before you make any changes after your code has been reviewed, you should always rebase the latest changes from the master branch.

After your contribution is merged, it’s not immediately available to all users. Your change will be shipped as part of the next release. If your change is time-sensitive, please let us know so we can schedule a release for your change.

<!-- Links -->
